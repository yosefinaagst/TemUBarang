-- =====================================================================
-- TemUBarang · F1 Laporan Barang Hilang (Naila Rania Shofa)  · versi 2
-- Jalankan di Supabase: SQL Editor → New query → tempel → Run.
--
-- Versi 2 menambah FOTO (opsional) untuk laporan hilang:
--   bucket Storage "foto-hilang" + kolom photo_path di function.
-- Aman dijalankan ulang, dan aman walaupun versi 1 sudah pernah dijalankan.
-- Policy dan function milik fitur lain (temuan, profil) tidak diubah.
-- =====================================================================

begin;

-- ---------------------------------------------------------------------
-- 1. Baca feed Jelajah · Hilang
--    Semua akun yang login boleh membaca laporan hilang yang belum dihapus.
-- ---------------------------------------------------------------------
drop policy if exists hilang_feed_read on public.reports;
create policy hilang_feed_read on public.reports
    for select to authenticated
    using (report_type = 'lost' and deleted_at is null);


-- ---------------------------------------------------------------------
-- 2. Storage foto laporan hilang
--    Bucket privat, JPG/PNG maks. 5 MB. File disimpan di folder milik
--    pengguna: <user_id>/<nama_file>. Hanya pemilik folder yang boleh
--    mengunggah, mengganti, dan menghapus. Semua akun login boleh melihat.
-- ---------------------------------------------------------------------
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('foto-hilang', 'foto-hilang', false, 5242880, array['image/jpeg', 'image/png'])
on conflict (id) do update
    set public = false,
        file_size_limit = excluded.file_size_limit,
        allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists foto_hilang_baca on storage.objects;
create policy foto_hilang_baca on storage.objects
    for select to authenticated
    using (bucket_id = 'foto-hilang');

drop policy if exists foto_hilang_unggah on storage.objects;
create policy foto_hilang_unggah on storage.objects
    for insert to authenticated
    with check (bucket_id = 'foto-hilang'
                and (storage.foldername(name))[1] = (select auth.uid())::text);

drop policy if exists foto_hilang_ubah on storage.objects;
create policy foto_hilang_ubah on storage.objects
    for update to authenticated
    using (bucket_id = 'foto-hilang'
           and (storage.foldername(name))[1] = (select auth.uid())::text)
    with check (bucket_id = 'foto-hilang'
                and (storage.foldername(name))[1] = (select auth.uid())::text);

drop policy if exists foto_hilang_hapus on storage.objects;
create policy foto_hilang_hapus on storage.objects
    for delete to authenticated
    using (bucket_id = 'foto-hilang'
           and (storage.foldername(name))[1] = (select auth.uid())::text);


-- ---------------------------------------------------------------------
-- Hapus function versi 1 (parameternya berbeda) supaya tidak dobel.
-- ---------------------------------------------------------------------
drop function if exists public.simpan_hilang(uuid, text, text, text, text, date, text);
drop function if exists public.perbarui_hilang(uuid, integer, text, text, text, text, date, text);


-- ---------------------------------------------------------------------
-- 3. Terbitkan laporan hilang (Insert)
--    Satu transaksi: profil dipastikan ada → laporan → riwayat awal.
--    p_id dibuat aplikasi, jadi kirim ulang setelah koneksi putus
--    tidak membuat laporan ganda.
--    p_foto opsional; jika diisi harus "<user_id>/<p_id>.jpg|png"
--    dan file-nya sudah ada di bucket foto-hilang.
-- ---------------------------------------------------------------------
create or replace function public.simpan_hilang(
    p_id uuid,
    p_nama text,
    p_kategori text,
    p_deskripsi text,
    p_lokasi text,
    p_tanggal date,
    p_ciri text default null,
    p_foto text default null
)
returns uuid
language plpgsql
security definer
set search_path = ''
as $$
declare
    uid uuid := auth.uid();
    existing public.reports%rowtype;
    kontak text;
    foto text := nullif(trim(coalesce(p_foto, '')), '');
begin
    if uid is null then
        raise exception 'Sesi berakhir. Silakan masuk kembali.';
    end if;
    if p_id is null then
        raise exception 'ID laporan tidak valid.';
    end if;
    if coalesce(trim(p_nama), '') = '' or coalesce(trim(p_deskripsi), '') = ''
       or coalesce(trim(p_lokasi), '') = '' then
        raise exception 'Lengkapi kolom wajib yang ditandai.';
    end if;
    if length(trim(p_nama)) > 80 then
        raise exception 'Nama barang maksimal 80 karakter.';
    end if;
    if length(trim(p_deskripsi)) > 500 then
        raise exception 'Deskripsi maksimal 500 karakter.';
    end if;
    if length(coalesce(trim(p_ciri), '')) > 300 then
        raise exception 'Ciri khusus maksimal 300 karakter.';
    end if;
    if p_kategori is null or p_kategori not in (
        'electronics', 'wallet_bag', 'keys', 'cards_documents',
        'accessories', 'books_stationery', 'personal_items', 'other'
    ) then
        raise exception 'Pilih kategori barang.';
    end if;
    if p_tanggal is null or p_tanggal > (now() at time zone 'Asia/Jakarta')::date then
        raise exception 'Tanggal kehilangan tidak boleh melewati hari ini.';
    end if;
    if foto is not null then
        if foto not in (uid::text || '/' || p_id::text || '.jpg',
                        uid::text || '/' || p_id::text || '.png') then
            raise exception 'Path foto tidak sesuai.';
        end if;
        if not exists (select 1 from storage.objects
                       where bucket_id = 'foto-hilang' and name = foto) then
            raise exception 'Foto belum terunggah. Coba lagi.';
        end if;
    end if;

    -- Cegah dua kiriman bersamaan dengan ID yang sama.
    perform pg_advisory_xact_lock(hashtextextended(p_id::text, 0));
    select * into existing from public.reports where id = p_id;
    if found then
        if existing.owner_id = uid and existing.report_type = 'lost'
           and existing.deleted_at is null then
            return p_id;  -- kiriman ulang: laporan sudah tersimpan
        end if;
        raise exception 'ID laporan sudah digunakan.';
    end if;

    -- Akun lama mungkin belum punya baris profil.
    insert into public.profiles (id, full_name)
    select uid, coalesce(nullif(trim(raw_user_meta_data ->> 'full_name'), ''), 'Pengguna Baru')
    from auth.users where id = uid
    on conflict (id) do nothing;

    select nullif(trim(contact), '') into kontak from public.profiles where id = uid;

    insert into public.reports (
        id, owner_id, report_type, item_name, category, description,
        public_characteristics, location, event_date, photo_path, contact_public, status
    ) values (
        p_id, uid, 'lost', trim(p_nama), p_kategori, trim(p_deskripsi),
        nullif(trim(p_ciri), ''), trim(p_lokasi), p_tanggal, foto, kontak, 'reported'
    );

    insert into public.status_history (report_id, actor_id, from_status, to_status, note)
    values (p_id, uid, null, 'reported', 'Laporan kehilangan diterbitkan.');

    return p_id;
end;
$$;


-- ---------------------------------------------------------------------
-- 4. Detail laporan hilang (Read) + nama & kontak pelapor
--    Tabel profiles tidak dibuka ke semua orang; yang dikirim hanya
--    nama dan kontak pelapor untuk laporan ini.
-- ---------------------------------------------------------------------
create or replace function public.detail_hilang(p_id uuid)
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
        'id', r.id,
        'nama', r.item_name,
        'kategori', r.category,
        'deskripsi', r.description,
        'ciri', coalesce(r.public_characteristics, ''),
        'lokasi', r.location,
        'tanggal', r.event_date,
        'foto', coalesce(r.photo_path, ''),
        'status', r.status,
        'versi', r.version,
        'milik_saya', r.owner_id = uid,
        'pelapor', coalesce(nullif(trim(p.full_name), ''), 'Pengguna'),
        'kontak', coalesce(nullif(trim(p.contact), ''), r.contact_public, '')
    )
    into hasil
    from public.reports r
    left join public.profiles p on p.id = r.owner_id
    where r.id = p_id and r.report_type = 'lost' and r.deleted_at is null;

    if hasil is null then
        raise exception 'Laporan ini sudah tidak tersedia.' using errcode = 'TB404';
    end if;
    return hasil;
end;
$$;


-- ---------------------------------------------------------------------
-- 5. Edit laporan hilang milik sendiri (Update)
--    p_versi = versi yang dibaca saat form dibuka. Kalau laporan sudah
--    diubah di perangkat lain, edit ditolak (errcode TB409).
--    Foto: p_foto = foto baru "<user_id>/<uuid>.jpg|png", atau
--          p_hapus_foto = true untuk menghapus foto. Keduanya kosong =
--          foto lama dipertahankan.
--    Mengembalikan { versi, foto_lama } — foto_lama diisi bila foto
--    diganti/dihapus, supaya aplikasi bisa membersihkan file lama.
--    Status TIDAK diubah di sini.
-- ---------------------------------------------------------------------
create or replace function public.perbarui_hilang(
    p_id uuid,
    p_versi integer,
    p_nama text,
    p_kategori text,
    p_deskripsi text,
    p_lokasi text,
    p_tanggal date,
    p_ciri text default null,
    p_foto text default null,
    p_hapus_foto boolean default false
)
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
    uid uuid := auth.uid();
    foto text := nullif(trim(coalesce(p_foto, '')), '');
    foto_lama text;
    versi_baru integer;
begin
    if uid is null then
        raise exception 'Sesi berakhir. Silakan masuk kembali.';
    end if;
    if coalesce(trim(p_nama), '') = '' or coalesce(trim(p_deskripsi), '') = ''
       or coalesce(trim(p_lokasi), '') = '' then
        raise exception 'Lengkapi kolom wajib yang ditandai.';
    end if;
    if length(trim(p_nama)) > 80 then
        raise exception 'Nama barang maksimal 80 karakter.';
    end if;
    if length(trim(p_deskripsi)) > 500 then
        raise exception 'Deskripsi maksimal 500 karakter.';
    end if;
    if length(coalesce(trim(p_ciri), '')) > 300 then
        raise exception 'Ciri khusus maksimal 300 karakter.';
    end if;
    if p_kategori is null or p_kategori not in (
        'electronics', 'wallet_bag', 'keys', 'cards_documents',
        'accessories', 'books_stationery', 'personal_items', 'other'
    ) then
        raise exception 'Pilih kategori barang.';
    end if;
    if p_tanggal is null or p_tanggal > (now() at time zone 'Asia/Jakarta')::date then
        raise exception 'Tanggal kehilangan tidak boleh melewati hari ini.';
    end if;
    if foto is not null then
        if foto !~ ('^' || uid::text || '/[0-9a-f-]{36}\.(jpg|png)$') then
            raise exception 'Path foto tidak sesuai.';
        end if;
        if not exists (select 1 from storage.objects
                       where bucket_id = 'foto-hilang' and name = foto) then
            raise exception 'Foto belum terunggah. Coba lagi.';
        end if;
    end if;

    select photo_path into foto_lama
    from public.reports
    where id = p_id and owner_id = uid and report_type = 'lost' and deleted_at is null
    for update;

    update public.reports
    set item_name = trim(p_nama),
        category = p_kategori,
        description = trim(p_deskripsi),
        public_characteristics = nullif(trim(p_ciri), ''),
        location = trim(p_lokasi),
        event_date = p_tanggal,
        photo_path = case
            when foto is not null then foto
            when p_hapus_foto then null
            else photo_path
        end,
        version = version + 1,
        updated_at = now()
    where id = p_id
      and owner_id = uid
      and report_type = 'lost'
      and deleted_at is null
      and version = p_versi
    returning version into versi_baru;

    if versi_baru is null then
        if exists (
            select 1 from public.reports
            where id = p_id and owner_id = uid and report_type = 'lost' and deleted_at is null
        ) then
            raise exception 'Laporan sudah diubah di perangkat lain. Muat ulang lalu coba lagi.'
                using errcode = 'TB409';
        end if;
        raise exception 'Laporan tidak ditemukan atau bukan milikmu.';
    end if;

    return jsonb_build_object(
        'versi', versi_baru,
        'foto_lama', case
            when (foto is not null or p_hapus_foto) and foto_lama is distinct from foto
                then foto_lama
        end
    );
end;
$$;


-- ---------------------------------------------------------------------
-- 6. Hapus laporan hilang milik sendiri (Delete · soft delete)
--    Baris tetap ada agar riwayat, klaim, dan bookmark tidak rusak;
--    laporan hanya disembunyikan dari Jelajah.
-- ---------------------------------------------------------------------
create or replace function public.hapus_hilang(p_id uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    uid uuid := auth.uid();
begin
    if uid is null then
        raise exception 'Sesi berakhir. Silakan masuk kembali.';
    end if;

    update public.reports
    set deleted_at = now(),
        updated_at = now(),
        version = version + 1
    where id = p_id
      and owner_id = uid
      and report_type = 'lost'
      and deleted_at is null;

    if not found then
        raise exception 'Laporan tidak ditemukan atau sudah dihapus.';
    end if;
end;
$$;


-- ---------------------------------------------------------------------
-- 7. Hak eksekusi: hanya akun yang login.
-- ---------------------------------------------------------------------
revoke execute on function public.simpan_hilang(uuid, text, text, text, text, date, text, text) from public, anon;
revoke execute on function public.detail_hilang(uuid) from public, anon;
revoke execute on function public.perbarui_hilang(uuid, integer, text, text, text, text, date, text, text, boolean) from public, anon;
revoke execute on function public.hapus_hilang(uuid) from public, anon;

grant execute on function public.simpan_hilang(uuid, text, text, text, text, date, text, text) to authenticated;
grant execute on function public.detail_hilang(uuid) to authenticated;
grant execute on function public.perbarui_hilang(uuid, integer, text, text, text, text, date, text, text, boolean) to authenticated;
grant execute on function public.hapus_hilang(uuid) to authenticated;

commit;

-- Minta API Supabase membaca ulang daftar function.
notify pgrst, 'reload schema';
