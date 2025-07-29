<p align="center">
  <img src="docs/icon.png" width="128" height="128" alt="Let's Troll Icon"/>
</p>

<h1 align="center">Let's Troll</h1>

<p align="center">
  A lightweight and fun Minecraft plugin for Paper servers that lets you go ghost mode, spawn scary armor stands, and bind troll actions to items.
</p>

<p align="center">
  <a href="https://github.com/IMDelewer/LetsTroll/releases"><img src="https://img.shields.io/github/v/release/IMDelewer/LetsTroll?label=version" alt="Latest Release" /></a>
  <a href="https://github.com/IMDelewer/LetsTroll/actions/workflows/release.yml"><img src="https://github.com/IMDelewer/LetsTroll/actions/workflows/release.yml/badge.svg" alt="Build Status"></a>
  <a href="https://github.com/IMDelewer/LetsTroll/issues"><img src="https://img.shields.io/github/issues/IMDelewer/LetsTroll" alt="Open Issues" /></a>
  <a href="https://github.com/IMDelewer/LetsTroll/pulls"><img src="https://img.shields.io/github/issues-pr/IMDelewer/LetsTroll" alt="Open Pull Requests" /></a>
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-blue" alt="Minecraft Version" />
  <a href="https://github.com/IMDelewer/LetsTroll/blob/main/LICENSE"><img src="https://img.shields.io/github/license/IMDelewer/LetsTroll" alt="License" /></a>
  <img src="https://img.shields.io/github/languages/top/IMDelewer/LetsTroll" alt="Top Language" />
  <img src="https://img.shields.io/github/last-commit/IMDelewer/LetsTroll" alt="Last Commit" />
</p>

---

## ✨ Features

- 🕵️ Ghost mode: become invisible and suppress join/leave messages  
- 🎭 Spawn scary customized armor stands as "scare actors"  
- 🔧 Bind troll actions (lightning, fake fall, explosions) to items  
- 🗂 Configurable armor stands via `stends.yml`  
- 🔄 Live config reloading with `/stand reload`  

---

## 🛠 Commands

| Command | Description |
|--------|-------------|
| `/ghost` | Toggle ghost mode for yourself |
| `/stand <name> <player>` | Spawn a pre-configured armor stand |
| `/stand reload` | Reload `stends.yml` |
| `/tool bind <action>` | Bind an action to the held item |
| `/tool unbind` | Remove bound action from the held item |
| `/tool stand <name>` | Bind armor stand spawning to the item |

> All commands require `OP` or specific permissions (`letstroll.*`)

---

## 🎥 Demo GIFs

| `Fake Fall`                      | `Fake Item`                      |
|----------------------------------|----------------------------------|
| ![Fake Fall](docs/Fake%20Fall.gif) | ![Fake Item](docs/Fake%20Item.gif) |

---

## 📦 Installation

1. Make sure your server runs **Paper 1.21.1**  
2. Download the latest `.jar` from the [Releases](https://github.com/IMDelewer/LetsTroll/releases)  
3. Place it in your `plugins/` folder  
4. Start the server — config files will be generated  

---

## ⚙️ Example Configuration (`stends.yml`)

```yaml
blue_stend:
  name: "Blue Stand"
  skin: "https://textures.minecraft.net/texture/..."
  chestplate: "LEATHER_CHESTPLATE"
  leggings: "LEATHER_LEGGINGS"
  boots: "LEATHER_BOOTS"
  color_chestplate: "#0000FF"
  color_leggings: "#0000FF"
  color_boots: "#0000FF"
````

---

## 🚀 Build from Source

This project uses **Maven**.

```bash
git clone https://github.com/IMDelewer/LetsTroll.git
cd LetsTroll
mvn clean package
```

The final `.jar` will be in the `target/` folder.

---

## 👤 Author

Created by [IMDelewer](https://github.com/IMDelewer)
Open issues or feature requests are welcome!

---

## 📄 License

This project is licensed under the MIT License.
See [LICENSE](LICENSE) for details.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!
Feel free to open a pull request or issue on GitHub.
If you want to collaborate or have ideas to improve Let's Troll, don't hesitate to reach out.

Together we can make this plugin even more fun and useful! 🚀