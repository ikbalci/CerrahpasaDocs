# Cerrahpaşa Docs

Cerrahpaşa Docs, çok kullanıcılı, gerçek zamanlı bir metin editörü masaüstü uygulamasıdır.

## Temel Özellikler

*   **Gerçek Zamanlı Ortak Düzenleme:** Birden fazla kullanıcı aynı anda aynı dosyalar üzerinde değişiklik yapabilir ve değişiklikleri eş zamanlı olarak görebilir.
*   **Kullanıcı Yönetimi:** Kullanıcılar sisteme benzersiz bir kullanıcı adı ile giriş yaparlar.
*   **Dosya Yönetimi:**
    *   Sunucudaki dosyaları listeleme.
    *   Yeni dosya oluşturma.
    *   Mevcut dosyaları açma ve farklı sekmelerde düzenleme.
    *   Değişikliklerin sunucu tarafında otomatik ve manuel olarak kaydedilmesi.
*   **Özel İletişim Protokolü:** İstemci ve sunucu arasında veri alışverişi için özel tasarlanmış metin tabanlı bir protokol kullanır.
*   **Yalın Soket Programlama:** İletişim, Java'nın temel TCP soketleri kullanılarak gerçekleştirilmiştir. Herhangi bir üçüncü parti ağ framework'ü kullanılmamıştır.
*   **Masaüstü Arayüzü:** Kullanıcı etkileşimi için Java Swing ile bir masaüstü arayüzü sunar.

## Kullanılan Teknolojiler

*   **Programlama Dili:** Java 21
*   **Arayüz:** Java Swing
*   **Ağ İletişimi:** TCP/IP Soketleri (java.net.Socket, java.net.ServerSocket)
*   **Protokol:** Özel Tasarım Metin Tabanlı Protokol
*   **Derleme ve Bağımlılık Yönetimi:** Apache Maven

## Gereksinimler

Projeyi derlemek ve çalıştırmak için sisteminizde aşağıdakilerin kurulu olması gerekmektedir:

*   Java Development Kit (JDK) 21 veya üzeri
*   Apache Maven 3.6.0 veya üzeri

## Derleme Talimatları

Projenin kök dizininde bir terminal veya komut istemcisi açın ve aşağıdaki Maven komutunu çalıştırın:

```bash
mvn clean package
```

## Uygulamayı Çalıştırma

Uygulama, bir ana kontrol paneli (`MainMenuFrame`) üzerinden hem sunucuyu başlatır hem de istemci pencerelerinin açılmasına olanak tanır.

**1. IDE Üzerinden Çalıştırma:**

Proje dosyalarını bir Java IDE'sine (örn: IntelliJ IDEA, Eclipse) import ettikten sonra `edu.iuc.Main` sınıfını bularak çalıştırabilirsiniz. Bu sınıf, `MainMenuFrame`'i başlatacaktır.

`MainMenuFrame` açıldığında sunucu otomatik olarak 9999 portunda başlatılacaktır. Ardından "Yeni Client Aç" butonu ile editör pencerelerini açıp kullanıcı girişi yaparak uygulamayı kullanmaya başlayabilirsiniz.

## Özel İletişim Protokolü

Uygulama, istemci ve sunucu arasındaki tüm iletişimi sağlamak için özel olarak tasarlanmış metin tabanlı bir protokol kullanır. Bu protokol, `KOMUT#PARAMETRE1#PARAMETRE2` şeklinde bir mesaj formatına sahiptir. Başlıca mesaj komutları arasında `LOGIN`, `LIST_FILES_REQUEST`, `OPEN_FILE_REQUEST`, `EDIT`, `CREATE_FILE`, `SAVE_FILE` ve bunlara karşılık gelen sunucu yanıtları (`SUCCESS`, `ERROR`, `LIST_FILES_RESPONSE` vb.) bulunur.

Protokol, mesaj başlığını (KOMUT) ve içeriğini (PARAMETRELER) ayırt ederek yapılandırılmış bir iletişim sağlar. Detaylı protokol dokümantasyonu proje raporunda sunulmuştur.
