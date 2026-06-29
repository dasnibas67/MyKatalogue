import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {
    // HashSet untuk menyimpan kata kunci terlarang (Kecepatan cek kata = O(1))
    private static final WordFilter wordFilter = new WordFilter();
    // HashMap untuk indeks pencarian agar cari barang langsung ketemu cepat
    private static final ProductManager productManager = new ProductManager(wordFilter, "products.json");
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Menu Utama Program (CLI).
     * Kompleksitas: Tergantung berapa kali user memilih menu (Event Loop).
     */
    public static void main(String[] args) {
        // Ambil data produk yang sudah tersimpan di file JSON saat program dibuka
        productManager.loadFromDisk();

        boolean running = true;
        // Loop 'while' ini menjaga agar program terus berjalan sampai user memilih menu 6
        while (running) {
            printHeader("MYKATALOGUE | PRODUCT SUMMARY MANAGER");
            System.out.println("1. Tambah Produk Baru");
            System.out.println("2. Lihat Katalog Produk (Terurut)");
            System.out.println("3. Cari Produk berdasarkan Kategori");
            System.out.println("4. Cari Produk berdasarkan Kata Kunci (Index Ringkasan)");
            System.out.println("5. Kelola Kata Kunci Terlarang (Forbidden Keywords)");
            System.out.println("6. Keluar");
            System.out.print("Pilih opsi (1-6): ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    tambahProduk();
                    break;
                case "2":
                    lihatKatalog();
                    break;
                case "3":
                    cariKategori();
                    break;
                case "4":
                    cariKataKunci();
                    break;
                case "5":
                    kelolaKataKunci();
                    break;
                case "6":
                    running = false;
                    System.out.println("\nTerima kasih telah menggunakan MyKatalogue!");
                    break;
                default:
                    System.out.println("\n[ERROR] Opsi tidak valid. Silakan coba lagi.");
            }
            if (running) {
                System.out.print("\nTekan ENTER untuk kembali ke menu utama...");
                scanner.nextLine();
            }
        }
    }

    /**
     * Menampilkan dekorasi judul menu.
     * Kompleksitas: O(1) karena hanya cetak teks biasa.
     */
    private static void printHeader(String title) {
        System.out.println("\n=======================================================================");
        System.out.println("                     " + title);
        System.out.println("=======================================================================");
    }

    /**
     * Menginput produk baru, cek sensor kata, lalu simpan ke memori & JSON.
     * Kompleksitas: O(N) karena harus menulis ulang semua data ke file JSON.
     */
    private static void tambahProduk() {
        printHeader("TAMBAH PRODUK BARU");
        
        System.out.print("Nama Produk: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Kategori   : ");
        String category = scanner.nextLine().trim();
        
        // Loop input harga agar aman dari input huruf/minus (Validasi Input)
        double price = 0;
        while (true) {
            System.out.print("Harga (Rp) : ");
            try {
                price = Double.parseDouble(scanner.nextLine().trim());
                if (price < 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Input harga harus berupa angka positif.");
            }
        }

        // Loop input rating agar aman dari input ngawur (Validasi Input)
        double rating = 0;
        while (true) {
            System.out.print("Rating(0-5): ");
            try {
                rating = Double.parseDouble(scanner.nextLine().trim());
                if (rating < 0 || rating > 5) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Input rating harus berupa angka desimal antara 0.0 sampai 5.0.");
            }
        }

        System.out.print("Ringkasan  : ");
        String summary = scanner.nextLine().trim();

        System.out.print("Deskripsi  : ");
        String description = scanner.nextLine().trim();

        // Cek apakah ada inputan yang kosong atau cuma spasi doang
        if (name.isEmpty() || category.isEmpty() || summary.isEmpty() || description.isEmpty()) {
            System.out.println("\n[ERROR] Semua field harus diisi dan tidak boleh hanya berisi spasi!");
            return;
        }

        // Proses Sensor: Cek apakah inputan mengandung kata terlarang (pakai HashSet & Regex)
        boolean hasForbidden = wordFilter.containsForbiddenWord(name) ||
                               wordFilter.containsForbiddenWord(summary) ||
                               wordFilter.containsForbiddenWord(description);

        // Jika terbukti ada kata palsu/kw/scam, penambahan langsung ditolak
        if (hasForbidden) {
            System.out.println("\n[ERROR] Penambahan produk dibatalkan! Sistem mendeteksi adanya kata kunci spam/terlarang.");
            return; 
        }

        // Buat objek produk baru dan masukkan ke dalam sistem
        Product p = new Product(name, category, price, rating, summary, description);
        productManager.addProduct(p); 

        System.out.println("\n[SUCCESS] Produk berhasil ditambahkan dan disimpan ke JSON!");
        System.out.println(p);
    }

    /**
     * Menu pilihan untuk mengurutkan katalog menggunakan Merge Sort.
     * Kompleksitas: O(N log N) karena kecepatan utama didominasi algoritma Merge Sort.
     */
    private static void lihatKatalog() {
        // Ambil semua daftar produk
        List<Product> products = productManager.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("\nKatalog produk masih kosong.");
            return;
        }

        printHeader("LIHAT KATALOG PRODUK");
        System.out.println("Pilih Parameter Pengurutan:");
        System.out.println("1. Rating Tertinggi (Descending)");
        System.out.println("2. Harga Termurah (Ascending)");
        System.out.println("3. Harga Termahal (Descending)");
        System.out.println("4. Alfabet Nama Produk (Ascending)");
        System.out.print("Pilih opsi (1-4): ");
        String sortOption = scanner.nextLine().trim();

        // Panggil SortingHelper berdasarkan menu urutan yang dipilih user
        switch (sortOption) {
            case "1":
                SortingHelper.mergeSort(products, SortingHelper.BY_RATING_DESC);
                System.out.println("\nKatalog Diurutkan berdasarkan: Rating Tertinggi");
                break;
            case "2":
                SortingHelper.mergeSort(products, SortingHelper.BY_PRICE_ASC);
                System.out.println("\nKatalog Diurutkan berdasarkan: Harga Termurah");
                break;
            case "3":
                SortingHelper.mergeSort(products, SortingHelper.BY_PRICE_DESC);
                System.out.println("\nKatalog Diurutkan berdasarkan: Harga Termahal");
                break;
            case "4":
                SortingHelper.mergeSort(products, SortingHelper.BY_NAME_ASC);
                System.out.println("\nKatalog Diurutkan berdasarkan: Alfabet Nama");
                break;
            default:
                System.out.println("\n[ERROR] Opsi tidak valid. Menampilkan tanpa pengurutan.");
        }

        // Tampilkan hasil tabel produk ke layar
        displayProductList(products);
    }

    /**
     * Fitur pencarian produk berdasarkan kategori.
     * Mengambil input kategori dari pengguna, lalu melakukan query ke ProductManager.
     * Hasil pencarian akan diurutkan berdasarkan rating tertinggi secara default.
     * 
     * ANALISIS KOMPLEKSITAS WAKTU:
     * - O(N log N)
     * Penjelasan:
     * N adalah jumlah produk dalam kategori yang ditemukan.
     * Mengambil produk dari indeks kategori membutuhkan waktu O(1) rata-rata.
     * Mengurutkan hasil pencarian menggunakan Merge Sort memakan waktu O(N log N) yang mendominasi keseluruhan fungsi ini.
     * Menampilkan daftar ke layar memakan waktu O(N).
     */
    private static void cariKategori() {
        printHeader("CARI PRODUK BERDASARKAN KATEGORI");
        System.out.print("Masukkan Kategori: ");
        String category = scanner.nextLine().trim();

        if (category.isEmpty()) {
            System.out.println("[ERROR] Kategori tidak boleh kosong.");
            return;
        }

        List<Product> results = productManager.getProductsByCategory(category);
        if (results.isEmpty()) {
            System.out.println("\nTidak ditemukan produk untuk kategori: \"" + category + "\".");
        } else {
            System.out.println("\nHasil pencarian kategori \"" + category + "\": (" + results.size() + " produk)");
            SortingHelper.mergeSort(results, SortingHelper.BY_RATING_DESC);
            displayProductList(results);
        }
    }

    /**
     * Fitur pencarian berdasarkan kata kunci pada ringkasan produk.
     * Memecah ringkasan menjadi kata-kata kunci dan mencarinya di index ringkasan produk.
     * Hasil pencarian akan ditampilkan dan diurutkan berdasarkan rating tertinggi secara default.
     * 
     * ANALISIS KOMPLEKSITAS WAKTU:
     * - O(N log N)
     * Penjelasan:
     * N adalah jumlah produk yang cocok dengan kata kunci hasil penelusuran.
     * Melakukan pemecahan query pencarian dan pencarian di HashMap indeks memakan waktu cepat.
     * Penggabungan produk unik ke dalam set dan pengurutan hasil akhir menggunakan Merge Sort memakan waktu O(N log N) yang mendominasi performa fungsi ini.
     */
    private static void cariKataKunci() {
        printHeader("CARI BERDASARKAN KATA KUNCI (SUMMARY INDEX)");
        System.out.print("Masukkan Kata Kunci: ");
        String query = scanner.nextLine().trim();

        if (query.isEmpty()) {
            System.out.println("[ERROR] Kata kunci tidak boleh kosong.");
            return;
        }

        List<Product> results = productManager.searchByKeyword(query);
        if (results.isEmpty()) {
            System.out.println("\nTidak ditemukan produk dengan kata kunci: \"" + query + "\".");
        } else {
            System.out.println("\nHasil pencarian kata kunci \"" + query + "\": (" + results.size() + " produk)");
            SortingHelper.mergeSort(results, SortingHelper.BY_RATING_DESC);
            displayProductList(results);
        }
    }

    /**
     * Menu interaktif untuk mengelola daftar kata kunci terlarang.
     * Pengguna dapat melihat daftar aktif, menambah kata kunci baru, atau menghapus kata kunci.
     * 
     * ANALISIS KOMPLEKSITAS WAKTU:
     * - O(N) per perubahan kata kunci
     * Penjelasan:
     * N adalah jumlah seluruh kata kunci terlarang aktif dalam filter.
     * Setiap kali ada penambahan atau penghapusan kata terlarang, sistem melakukan kompilasi ulang Regex Pattern 
     * secara linear terhadap total kata kunci aktif O(N).
     */
    private static void kelolaKataKunci() {
        boolean subRunning = true;
        while (subRunning) {
            printHeader("KELOLA KATA KUNCI TERLARANG (SPAM FILTER)");
            Set<String> words = wordFilter.getForbiddenWords();
            System.out.println("Daftar Kata Kunci Aktif: " + words);
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("1. Tambah Kata Kunci Terlarang");
            System.out.println("2. Hapus Kata Kunci Terlarang");
            System.out.println("3. Kembali ke Menu Utama");
            System.out.print("Pilih opsi (1-3): ");
            String opt = scanner.nextLine().trim();

            switch (opt) {
                case "1":
                    System.out.print("Masukkan kata kunci baru: ");
                    String newWord = scanner.nextLine().trim();
                    if (!newWord.isEmpty()) {
                        wordFilter.addForbiddenWord(newWord);
                        System.out.println("[SUCCESS] Kata \"" + newWord + "\" ditambahkan ke daftar terlarang.");
                    } else {
                        System.out.println("[ERROR] Input tidak boleh kosong.");
                    }
                    break;
                case "2":
                    System.out.print("Masukkan kata kunci yang ingin dihapus: ");
                    String remWord = scanner.nextLine().trim();
                    if (words.contains(remWord.toLowerCase())) {
                        wordFilter.removeForbiddenWord(remWord);
                        System.out.println("[SUCCESS] Kata \"" + remWord + "\" berhasil dihapus.");
                    } else {
                        System.out.println("[ERROR] Kata tidak ditemukan dalam daftar.");
                    }
                    break;
                case "3":
                    subRunning = false;
                    break;
                default:
                    System.out.println("[ERROR] Opsi tidak valid.");
            }
        }
    }

    /**
     * Menampilkan daftar produk ke dalam format tabel.
     * Pengguna juga diberi opsi untuk memilih nomor produk tertentu guna melihat informasi detail produk.
     * 
     * ANALISIS KOMPLEKSITAS WAKTU:
     * - O(N)
     * Penjelasan:
     * N adalah jumlah produk di dalam list yang dilewatkan.
     * Fungsi melakukan iterasi linear sebanyak N kali untuk mencetak setiap baris produk.
     */
    private static void displayProductList(List<Product> list) {
        System.out.printf("\n%-4s | %-25s | %-12s | %-15s | %-6s\n", "No", "Nama Produk", "Kategori", "Harga", "Rating");
        System.out.println("-----------------------------------------------------------------------");
        for (int i = 0; i < list.size(); i++) {
            Product p = list.get(i);
            String dispName = p.getName().length() > 23 ? p.getName().substring(0, 20) + "..." : p.getName();
            System.out.printf("%-4d | %-25s | %-12s | Rp %,15.2f | %-6.1f\n",
                (i + 1), dispName, p.getCategory(), p.getPrice(), p.getRating()
            );
        }
        System.out.println("-----------------------------------------------------------------------");
        System.out.print("Lihat rincian produk nomor berapa? (0 untuk batalkan): ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim());
            if (index > 0 && index <= list.size()) {
                System.out.println(list.get(index - 1));
            } else if (index != 0) {
                System.out.println("[ERROR] Nomor produk tidak valid.");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Input harus berupa angka.");
        }
    }
}
