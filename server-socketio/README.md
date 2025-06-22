# Cerrahpaşa Docs - Socket.IO Server

Bu sunucu, Cerrahpaşa Docs uygulamasının Socket.IO ile modernize edilmiş versiyonudur.

## Kurulum

1. Node.js'in yüklü olduğundan emin olun (versiyon 16 veya üstü)
2. Bu dizinde terminali açın
3. Bağımlılıkları yükleyin:
   ```bash
   npm install
   ```

## Çalıştırma

### Geliştirme Modu
```bash
npm run dev
```

### Üretim Modu  
```bash
npm start
```

## Özellikler

- **Port**: 9999
- **CORS**: Tüm origin'lere izin veriliyor
- **Protokol**: Mevcut CTP protokolü korunuyor
- **Dosya Depolama**: `files/` klasöründe
- **Real-time**: Socket.IO ile gerçek zamanlı iletişim

## API/Events

### İstemciden Sunucuya
- `message`: Tüm CTP protokol mesajları bu event ile gönderilir

### Sunucudan İstemciye  
- `message`: Tüm CTP protokol yanıtları bu event ile alınır
- `connect`: Bağlantı kurulduğunda
- `disconnect`: Bağlantı kesildiğinde

## Dosya Yapısı

```
server-socketio/
├── server.js          # Ana sunucu dosyası
├── package.json       # NPM yapılandırması
├── files/             # Kullanıcı dosyaları (otomatik oluşur)
└── README.md          # Bu dosya
```

## Protokol Uyumluluğu

Sunucu, mevcut CerrahText Protocol (CTP) formatını korur:
```
COMMAND#PARAM1#PARAM2
```

Tüm mevcut komutlar desteklenir:
- LOGIN
- LIST_FILES_REQUEST
- OPEN_FILE_REQUEST  
- CREATE_FILE
- EDIT
- SAVE_FILE

## Loglama

Sunucu şu bilgileri loglar:
- Yeni bağlantılar
- Kullanıcı giriş/çıkışları
- Dosya işlemleri
- Hata durumları 