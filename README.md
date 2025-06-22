# Cerrahpaşa Docs - Socket.IO Versiyonu

Bu proje, çok kullanıcılı gerçek zamanlı metin editörüdür. Önceden salt TCP socket programlama kullanırken, şimdi modern Socket.IO teknolojisi ile çalışmaktadır.

## Özellikler

- ✅ Çok kullanıcılı gerçek zamanlı düzenleme
- ✅ Dosya oluşturma, açma, kaydetme
- ✅ Kullanıcı giriş/çıkış bildirimleri  
- ✅ Modern Socket.IO iletişimi
- ✅ Mevcut CTP protokolü korunuyor
- ✅ Java Swing GUI
- ✅ Node.js sunucu

## Teknoloji Stack

### İstemci (Client)
- **Java 21** - Ana uygulama dili
- **Swing** - GUI framework
- **Socket.IO Client Java** - Gerçek zamanlı iletişim
- **Maven** - Bağımlılık yönetimi

### Sunucu (Server)  
- **Node.js** - Server runtime
- **Socket.IO** - WebSocket/polling tabanlı iletişim
- **fs-extra** - Dosya işlemleri

## Kurulum ve Çalıştırma

### 1. Ön Koşullar
- Java 21 veya üstü
- Node.js 16 veya üstü
- Maven 3.6 veya üstü

### 2. Sunucuyu Başlatın

```bash
# Sunucu klasörüne gidin
cd server-socketio

# Bağımlılıkları yükleyin
npm install

# Sunucuyu başlatın
npm start
```

### 3. İstemciyi Çalıştırın

```bash
# Ana projede
mvn compile exec:java -Dexec.mainClass="edu.iuc.Main"
```

### 4. Alternatif: IDE'den Çalıştırma

1. Main.java dosyasını IDE'nizde açın
2. Önce server-socketio klasöründe `npm install` çalıştırın
3. IDE'den Main sınıfını çalıştırın
4. Ana menüden "Yeni Client Aç" butonunu kullanın

## Protokol (CTP - CerrahText Protocol)

Mevcut mesaj formatı korunmuştur:

```
COMMAND#PARAM1#PARAM2
```

### Desteklenen Komutlar

| Komut | Açıklama |
|-------|----------|
| LOGIN | Kullanıcı girişi |
| LIST_FILES_REQUEST | Dosya listesi talebi |
| OPEN_FILE_REQUEST | Dosya açma |
| CREATE_FILE | Yeni dosya oluşturma |
| EDIT | Dosya düzenleme (gerçek zamanlı) |
| SAVE_FILE | Dosya kaydetme |

## Değişiklikler (v1 → v2)

### ✅ Avantajlar
- **Modern teknoloji**: Socket.IO WebSocket tabanlı iletişim
- **Daha iyi performans**: Otomatik reconnection, buffering
- **Kolay geliştirme**: Event-based yapı
- **Cross-platform**: Web tarayıcılardan da bağlanılabilir
- **Esnek**: HTTP polling fallback desteği

### 🔄 Korunan Özellikler  
- Tüm GUI aynı
- Mevcut protokol formatı
- Kullanıcı deneyimi
- Dosya yönetimi

## Proje Yapısı

```
CerrahpasaDocs/
├── src/main/java/edu/iuc/
│   ├── client/                    # İstemci kodları
│   │   ├── EditorFrame.java       # Ana editör penceresi
│   │   ├── MainMenuFrame.java     # Ana menü
│   │   └── SocketIOClientAdapter.java # Socket.IO adaptörü
│   ├── shared/                    # Ortak sınıflar
│   │   ├── Message.java           # Mesaj sınıfı
│   │   └── MessageType.java       # Mesaj türleri
│   └── Main.java                  # Ana giriş noktası
├── server-socketio/               # Node.js sunucu
│   ├── server.js                  # Socket.IO sunucusu
│   ├── package.json              # NPM konfigürasyonu
│   └── files/                    # Kullanıcı dosyaları
└── pom.xml                       # Maven konfigürasyonu
```
