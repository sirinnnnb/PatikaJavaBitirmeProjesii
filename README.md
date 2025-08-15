Library Management System (Java Console) 
Bu proje, Java dili kullanılarak geliştirilen basit bir Kütüphane Yönetim Sistemi uygulamasıdır. 
Amaç, konsol/terminal tabanlı bir arayüzle kitap ekleme, listeleme, arama, ödünç alma ve iade işlemlerini gerçekleştirmektir.  
Eklenen kitaplar kalıcı olarak `data/books.tsv` dosyasında saklanır, böylece program kapatılıp tekrar açıldığında veriler kaybolmaz. 

Özellikler  
- **Kitap ekleme**: Başlık, yazar ve yayın yılı bilgisi ile yeni kitap eklenebilir.
- **Kitap listeleme**: Kütüphanedeki tüm kitapları görüntüleme.
- **Başlığa göre arama**: Kitap başlığına göre arama yapma (büyük/küçük harf duyarsız).
- **Ödünç alma**: Bir kitap ödünç alınabilir, zaten ödünçteyse uyarı verir.
- **İade etme**: Ödünç alınmış bir kitabı iade etme.
- **Veri kalıcılığı**: Kitaplar `data/books.tsv` dosyasına kaydedilir.
- **Menü tabanlı kullanım**: Her işlem sonrası ana menüye dönüş yapılır.  

-   Kullanılan Teknolojiler Java (OOP prensipleri, sınıf ve nesne yapısı)
-   ArrayList (koleksiyon yönetimi)
-   Scanner (konsol girdi/çıktı işlemleri)
-   java.nio.file ve java.io (dosya okuma/yazma)
-   TSV formatı (tab ile ayrılmış veri saklama) 
