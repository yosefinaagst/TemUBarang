-- =====================================================================
-- TemUBarang · F1 Halaman 2 · Laporan Saya (Siti Rahma Nurhaibah)
-- Jalankan di Supabase SETELAH f1_laporan_hilang.sql:
--   SQL Editor → New query → tempel → Run.
--
-- Isi:
--   1. jumlah_laporan_saya()      → jumlah per tab (Dilaporkan, Dalam Proses, Selesai)
--   2. riwayat_hilang(p_id)       → riwayat status laporan milik sendiri (terbaru di atas)
--   3. ubah_status_hilang(...)    → update status + insert riwayat dalam SATU transaksi
-- Daftar per tab dibaca langsung dari tabel reports (policy hilang_feed_read
-- dari file F1 Halaman 1) dengan filter owner_id = akun aktif.
-- Edit dan hapus memakai function perbarui_hilang / hapus_hilang yang sudah ada.
-- Aman dijalankan ulang.
-- =====================================================================

begin;

-- ---------------------------------------------------------------------
-- 1. Jumlah laporan hilang milik akun aktif per status (angka di tab)
-- ---------------------------------------------------------------------
create or replace function public.jumlah_laporan_saya()
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
    uid uuid := auth.uid();
    hasil jsonb;
begin
    if uid is null then
        raise exception 'Sesi berakhir. Silakan masuk kembali.';
    end if;

    select jsonb_build_object(
        'reported',    count(*) filter (where status = 'reported'),
        'in_progress', count(*) filter (where status = 'in_progress'),
        'completed',   count(*) filter (where status = 'completed')
    )
    into hasil
    from public.reports
    where owner_id = uid and report_type = 'lost' and deleted_at is null;

    return hasil;
end;
$$;


-- ---------------------------------------------------------------------
-- 2. Riwayat status (hanya pemilik laporan), terbaru di atas
-- ---------------------------------------------------------------------
create or replace function public.riwayat_hilang(p_id uuid)
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
    uid uuid := auth.uid();
    hasil jsonb;
begin
    if uid is null then
        raise exception 'Sesi berakhir. Silakan masuk kembali.';
    end if;

    if not exists (
        select 1 from public.reports
        where id = p_id and owner_id = uid and report_type = 'lost' and deleted_at is null
    ) then
        raise exception 'Laporan ini sudah tidak tersedia.' using errcode = 'TB404';
    end if;

    select coalesce(jsonb_agg(jsonb_build_object(
               'dari', coalesce(h.from_status, ''),
               'ke', h.to_status,
               'catatan', coalesce(h.note, ''),
               'waktu', h.created_at
           ) order by h.created_at desc), '[]'::jsonb)
    into hasil
    from public.status_history h
    where h.report_id = p_id;

    return hasil;
end;
$$;


-- ---------------------------------------------------------------------
-- 3. Ubah status laporan hilang milik sendiri
--    Aturan (PRD F1 Halaman 2):
--      Dilaporkan   → Dalam Proses | Selesai
--      Dalam Proses → Dilaporkan (verifikasi gagal) | Selesai
--      Selesai      → terminal (tidak bisa diubah lagi)
--    Catatan wajib min. 5 karakter (maks. 300).
--    p_versi = versi saat form dibuka; beda versi → TB409.
--    Update status dan insert riwayat ada di function yang sama, jadi
--    satu transaksi: jika insert riwayat gagal, status tidak berubah.
-- ---------------------------------------------------------------------
create or replace function public.ubah_status_hilang(
    p_id uuid,
    p_versi integer,
    p_status text,
    p_catatan text
)
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
    uid uuid := auth.uid();
    laporan public.reports%rowtype;
    catatan text := trim(coalesce(p_catatan, ''));
    versi_baru integer;
begin
    if uid is null then
        raise exception 'Sesi berakhir. Silakan masuk kembali.';
    end if;
    if p_status is null or p_status not in ('reported', 'in_progress', 'completed') then
        raise exception 'Pilih status baru.';
    end if;
    if length(catatan) < 5 then
        raise exception 'Catatan minimal 5 karakter.';
    end if;
    if length(catatan) > 300 then
        raise exception 'Catatan maksimal 300 karakter.';
    end if;

    select * into laporan
    from public.reports
    where id = p_id and owner_id = uid and report_type = 'lost' and deleted_at is null
    for update;

    if not found then
        raise exception 'Laporan tidak ditemukan atau bukan milikmu.';
    end if;
    if laporan.version <> p_versi then
        raise exception 'Laporan sudah diubah di perangkat lain. Muat ulang lalu coba lagi.'
            using errcode = 'TB409';
    end if;
    if laporan.status = 'completed' then
        raise exception 'Laporan sudah selesai dan tidak bisa diubah lagi.';
    end if;
    if laporan.status = p_status then
        raise exception 'Pilih status yang berbeda dari status saat ini.';
    end if;

    update public.reports
    set status = p_status,
        version = version + 1,
        updated_at = now()
    where id = p_id
    returning version into versi_baru;

    insert into public.status_history (report_id, actor_id, from_status, to_status, note)
    values (p_id, uid, laporan.status, p_status, catatan);

    return jsonb_build_object('versi', versi_baru, 'status', p_status);
end;
$$;


-- ---------------------------------------------------------------------
-- 4. Hak eksekusi: hanya akun yang login.
-- ---------------------------------------------------------------------
revoke execute on function public.jumlah_laporan_saya() from public, anon;
revoke execute on function public.riwayat_hilang(uuid) from public, anon;
revoke execute on function public.ubah_status_hilang(uuid, integer, text, text) from public, anon;

grant execute on function public.jumlah_laporan_saya() to authenticated;
grant execute on function public.riwayat_hilang(uuid) to authenticated;
grant execute on function public.ubah_status_hilang(uuid, integer, text, text) to authenticated;

commit;

notify pgrst, 'reload schema';
