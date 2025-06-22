const { Server } = require('socket.io');
const fs = require('fs-extra');
const path = require('path');

const io = new Server(9999, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"]
  }
});

// Dosya yöneticisi
class FileManager {
  constructor() {
    this.filesPath = path.join(__dirname, 'files');
    this.ensureFilesDirectory();
  }

  async ensureFilesDirectory() {
    try {
      await fs.ensureDir(this.filesPath);
    } catch (error) {
      console.error('Files klasörü oluşturulamadı:', error.message);
    }
  }

  async saveFile(fileName, content) {
    if (!fileName || fileName.trim() === '') {
      throw new Error('Dosya adı boş olamaz');
    }

    if (fileName.includes('..') || fileName.includes('/') || fileName.includes('\\')) {
      throw new Error('Geçersiz dosya adı: ' + fileName);
    }

    const filePath = path.join(this.filesPath, fileName);
    const processedContent = content.replace(/\\n/g, '\n');
    await fs.writeFile(filePath, processedContent, 'utf8');
  }

  async loadFile(fileName) {
    if (!fileName || fileName.trim() === '') {
      throw new Error('Dosya adı boş olamaz');
    }

    if (fileName.includes('..') || fileName.includes('/') || fileName.includes('\\')) {
      throw new Error('Geçersiz dosya adı: ' + fileName);
    }

    const filePath = path.join(this.filesPath, fileName);
    if (!await fs.pathExists(filePath)) {
      throw new Error('Dosya bulunamadı: ' + fileName);
    }

    return await fs.readFile(filePath, 'utf8');
  }

  async listFiles() {
    try {
      const files = await fs.readdir(this.filesPath);
      return files.filter(file => {
        const filePath = path.join(this.filesPath, file);
        return fs.statSync(filePath).isFile();
      });
    } catch (error) {
      console.error('Dosyalar listelenirken hata:', error.message);
      return [];
    }
  }

  async createFile(fileName) {
    try {
      if (!fileName || fileName.trim() === '') {
        return false;
      }

      if (fileName.includes('..') || fileName.includes('/') || fileName.includes('\\')) {
        return false;
      }

      const filePath = path.join(this.filesPath, fileName);
      if (await fs.pathExists(filePath)) {
        return false;
      }

      await fs.writeFile(filePath, '', 'utf8');
      return true;
    } catch (error) {
      console.error('Dosya oluşturulamadı:', fileName, '-', error.message);
      return false;
    }
  }

  async fileExists(fileName) {
    if (!fileName || fileName.trim() === '') {
      return false;
    }

    try {
      const filePath = path.join(this.filesPath, fileName);
      return await fs.pathExists(filePath);
    } catch (error) {
      return false;
    }
  }
}

// Kullanıcı yöneticisi
class UserManager {
  constructor() {
    this.connectedUsers = new Map(); // username -> socket
    this.allClients = new Set(); // tüm socket'ler
  }

  addUser(username, socket) {
    if (!username || username.trim() === '') {
      return false;
    }

    if (this.connectedUsers.has(username)) {
      return false;
    }

    this.connectedUsers.set(username, socket);
    this.allClients.add(socket);

    // Diğer kullanıcılara yeni kullanıcının katıldığını bildir
    this.broadcastToOthers(this.createMessage('USER_JOINED', username, ''), socket);

    return true;
  }

  removeUser(socket) {
    const username = this.getUsernameBySocket(socket);
    if (username) {
      this.connectedUsers.delete(username);
      // Diğer kullanıcılara kullanıcının ayrıldığını bildir
      this.broadcastToOthers(this.createMessage('USER_LEFT', username, ''), socket);
    }

    this.allClients.delete(socket);
  }

  getUsernameBySocket(socket) {
    for (const [username, userSocket] of this.connectedUsers.entries()) {
      if (userSocket === socket) {
        return username;
      }
    }
    return null;
  }

  broadcastToAll(message) {
    for (const client of this.allClients) {
      client.emit('message', message);
    }
  }

  broadcastToOthers(message, sender) {
    for (const client of this.allClients) {
      if (client !== sender) {
        client.emit('message', message);
      }
    }
  }

  createMessage(type, param1 = '', param2 = '') {
    return `${type}#${param1}#${param2}`;
  }

  getUserCount() {
    return this.connectedUsers.size;
  }

  getConnectedUsernames() {
    return Array.from(this.connectedUsers.keys());
  }
}

// Global nesneler
const fileManager = new FileManager();
const userManager = new UserManager();

// Message parser
class MessageParser {
  static parse(raw) {
    if (!raw || raw.trim() === '') {
      return ['', '', ''];
    }

    const parts = raw.split('#', 3);
    return [
      parts.length > 0 ? parts[0].trim() : '',
      parts.length > 1 ? parts[1].trim() : '',
      parts.length > 2 ? parts[2] : ''
    ];
  }
}

// Socket.IO bağlantı yönetimi
io.on('connection', (socket) => {
  console.log(`[${new Date().toLocaleTimeString()}] Yeni bağlantı: ${socket.id}`);
  
  let username = null;
  let isLoggedIn = false;

  // Mesaj işleme
  socket.on('message', async (rawMessage) => {
    try {
      await processMessage(rawMessage, socket);
    } catch (error) {
      console.error('Mesaj işlenirken hata:', error);
      socket.emit('message', userManager.createMessage('ERROR', 'PROCESSING_ERROR', error.message));
    }
  });

  // Bağlantı kesilme
  socket.on('disconnect', () => {
    console.log(`[${new Date().toLocaleTimeString()}] Bağlantı kesildi: ${socket.id} (${username || 'Anonim'})`);
    userManager.removeUser(socket);
  });

  // Mesaj işleme fonksiyonu
  async function processMessage(rawMessage, socket) {
    const [command, param1, param2] = MessageParser.parse(rawMessage);

    if (!command) {
      socket.emit('message', userManager.createMessage('ERROR', 'INVALID_MESSAGE', 'Geçersiz mesaj formatı'));
      return;
    }

    // Giriş kontrolü (LOGIN hariç)
    if (command !== 'LOGIN' && !isLoggedIn) {
      socket.emit('message', userManager.createMessage('ERROR', 'NOT_LOGGED_IN', 'Önce giriş yapmanız gerekir'));
      return;
    }

    switch (command) {
      case 'LOGIN':
        await handleLogin(param1, socket);
        break;

      case 'LIST_FILES_REQUEST':
        await handleListFilesRequest(socket);
        break;

      case 'OPEN_FILE_REQUEST':
        await handleOpenFileRequest(param1, socket);
        break;

      case 'EDIT':
        await handleEdit(param1, param2, socket);
        break;

      case 'CREATE_FILE':
        await handleCreateFile(param1, socket);
        break;

      case 'SAVE_FILE':
        await handleSaveFile(param1, param2, socket);
        break;

      default:
        socket.emit('message', userManager.createMessage('ERROR', 'UNKNOWN_COMMAND', 'Bilinmeyen komut: ' + command));
    }
  }

  // Komut işleyicileri
  async function handleLogin(requestedUsername, socket) {
    if (isLoggedIn) {
      socket.emit('message', userManager.createMessage('ERROR', 'ALREADY_LOGGED_IN', 'Zaten giriş yapılmış'));
      return;
    }

    if (!requestedUsername || requestedUsername.trim() === '') {
      socket.emit('message', userManager.createMessage('ERROR', 'INVALID_USERNAME', 'Geçersiz kullanıcı adı'));
      return;
    }

    if (userManager.addUser(requestedUsername, socket)) {
      username = requestedUsername;
      isLoggedIn = true;
      socket.emit('message', userManager.createMessage('SUCCESS', 'Giriş başarılı', ''));
      
      console.log(`[${new Date().toLocaleTimeString()}] Kullanıcı giriş yaptı: ${username}`);
      
      // Dosya listesini gönder
      await handleListFilesRequest(socket);
    } else {
      socket.emit('message', userManager.createMessage('ERROR', 'USERNAME_TAKEN', 'Bu kullanıcı adı zaten kullanılıyor'));
    }
  }

  async function handleListFilesRequest(socket) {
    try {
      const files = await fileManager.listFiles();
      const fileList = files.join(',');
      socket.emit('message', userManager.createMessage('LIST_FILES_RESPONSE', fileList, ''));
    } catch (error) {
      socket.emit('message', userManager.createMessage('ERROR', 'LIST_ERROR', error.message));
    }
  }

  async function handleOpenFileRequest(fileName, socket) {
    try {
      const content = await fileManager.loadFile(fileName);
      socket.emit('message', userManager.createMessage('OPEN_FILE_RESPONSE', fileName, content));
    } catch (error) {
      socket.emit('message', userManager.createMessage('ERROR', 'FILE_ERROR', error.message));
    }
  }

  async function handleEdit(fileName, content, socket) {
    try {
      await fileManager.saveFile(fileName, content);
      
      // Diğer kullanıcılara değişikliği bildir
      const editMessage = userManager.createMessage('EDIT', fileName, content);
      userManager.broadcastToOthers(editMessage, socket);
      
    } catch (error) {
      socket.emit('message', userManager.createMessage('ERROR', 'SAVE_ERROR', error.message));
    }
  }

  async function handleCreateFile(fileName, socket) {
    try {
      const success = await fileManager.createFile(fileName);
      if (success) {
        socket.emit('message', userManager.createMessage('SUCCESS', 'Dosya oluşturuldu: ' + fileName, ''));
        
        // Tüm kullanıcılara dosya listesini güncelle
        await handleListFilesRequest(socket);
        userManager.broadcastToOthers(userManager.createMessage('LIST_FILES_REQUEST', '', ''), socket);
      } else {
        socket.emit('message', userManager.createMessage('ERROR', 'CREATE_ERROR', 'Dosya oluşturulamadı (zaten var olabilir)'));
      }
    } catch (error) {
      socket.emit('message', userManager.createMessage('ERROR', 'CREATE_ERROR', error.message));
    }
  }

  async function handleSaveFile(fileName, content, socket) {
    try {
      await fileManager.saveFile(fileName, content);
      socket.emit('message', userManager.createMessage('SUCCESS', 'Dosya kaydedildi: ' + fileName, ''));
    } catch (error) {
      socket.emit('message', userManager.createMessage('ERROR', 'SAVE_ERROR', error.message));
    }
  }
});

console.log(`[${new Date().toLocaleTimeString()}] Cerrahpaşa Docs Socket.IO Sunucusu başlatıldı`);
console.log(`[${new Date().toLocaleTimeString()}] Port: 9999`);
console.log(`[${new Date().toLocaleTimeString()}] CORS: Tüm origin'lere izin veriliyor`); 