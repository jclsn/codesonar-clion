# codesonar-clion

CodeSonar warnings in CLion, from a CodeSonar hub or an exported SARIF file.

The work is done by the [codesonar-lsp] language server: it talks to the
hub, moves warnings along with the code while it is edited and hides
warnings whose code changed since the analysis. This plugin starts the
server with the LSP support of the IDE and adds what LSP has no counterpart
for:

- choose, show, hide and delete the analyses of the hub project
- resolve warnings on the hub as fixed or as false positives
- disable the check of a warning in `codesonar.conf`
- explain warnings in a popup, with the events that lead to them and the
  rules they may violate
- run `codesonar analyze` for the workspace, incrementally, with the hub,
  project, presets and checks of its `codesonar.conf`
- choose the presets and checks of analyses in a dialog
- list the shown warnings in a tool window and open them on the hub
- show the analysis and what is loading in the status bar

The warnings are regular diagnostics of the IDE: they are highlighted in
the editor, explained on hover and listed in the Problems tool window.
`Alt+Enter` on a warning offers its quick fixes, "Show details", "Open
in CodeSonar Hub", for warnings of a hub "Mark as fixed" and "Mark as false
positive", and "Disable check".

[codesonar-lsp]: https://github.com/jclsn/codesonar-lsp
[server-releases]: https://github.com/jclsn/codesonar-lsp/releases

## Installation

Requirements: CLion 2026.3 or newer. Running analyses also needs the
CodeSonar command line tools in your `PATH`.

1. Install the `codesonar-lsp` server. Download it from its
   [releases][server-releases]; the packages install on any release of
   Debian, Ubuntu, RHEL or AlmaLinux:

   ```sh
   sudo apt install ./codesonar-lsp_0.1.0-1_amd64.deb    # Debian, Ubuntu
   sudo dnf install ./codesonar-lsp-0.1.0-1.x86_64.rpm   # RHEL, AlmaLinux
   ```

   On other systems, put the binary of the `.tar.gz` (Linux) or the `.zip`
   (Windows) into a directory in your `PATH`. Or build it with `cargo
   install --path .` in its repository, which installs it into
   `~/.cargo/bin`.

2. Download the ZIP file of the plugin, `codesonar-clion-<version>.zip`,
   from the [releases](https://github.com/jclsn/codesonar-clion/releases).
   Do not unpack it.

3. In CLion, open **Settings | Plugins**, click the gear icon, choose
   **Install Plugin from Disk...** and select the ZIP file. Restart CLion
   when it asks.

4. Open **Settings | Tools | CodeSonar** and enter your hub user and
   password, see [Setup](#setup). If the server is not in your `PATH` or
   in `~/.cargo/bin`, enter its path there as well.

5. Open a C or C++ file of your project. The status bar shows `CodeSonar`
   and then the analysis that is shown. If your project has no
   `codesonar.conf` yet, run **Tools | CodeSonar | Workspace
   Configuration** to create it.

To update the plugin, install the new ZIP file the same way. To remove it,
disable or uninstall "CodeSonar" in **Settings | Plugins | Installed**.

### Building from source

```sh
./gradlew buildPlugin
```

The ZIP file is in `build/distributions`. The build downloads CLion. To
build against an installed one instead, pass its path, or set `CLION_HOME`:

```sh
./gradlew buildPlugin -PclionPath=/opt/clion
```

## Setup

The server is looked up in `$PATH` and in `~/.cargo/bin`. It starts with
the first C or C++ file of a workspace: the closest directory above the
file with a `codesonar.conf`, a `.codesonar` or a `.git`, else the project
directory.

**Settings | Tools | CodeSonar** has the settings of the server. Usually
only the hub credentials are needed:

| Setting             | Description                                                     |
| ------------------- | --------------------------------------------------------------- |
| Server              | Path of `codesonar-lsp`, or its name in `$PATH`.                |
| URL                 | Hub URL, if `codesonar.conf` has no `HUB_ADDRESS`, or to use `https`. |
| User                | Hub user. Empty for anonymous access.                           |
| Password            | Hub password, kept in the password store of the IDE.            |
| Password file       | File with the password instead. Without either, `$CODESONAR_PASSWORD` is used. |
| Project             | Hub project, if `codesonar.conf` names none.                    |
| SARIF file          | SARIF file to show instead of a hub, relative to the workspace root. |
| Show warnings in changed code as hints | They are hidden otherwise, since they are usually fixed. |
| Additional settings | Further settings of the server as a JSON object, e.g. `{"cachedAnalyses": 5, "pathMappings": [{"from": "/builds/app", "to": "/home/me/src/app"}]}`. |

The fields win over the same settings in the JSON. Applying the settings
restarts the server. See the reference of codesonar-lsp for all settings.

The hub, the project and how analyses run come from the `codesonar.conf`
of each workspace: open a file of your project and run **Tools | CodeSonar
| Workspace Configuration**. It asks for the hub address, the hub project
and the build commands and writes them into the file.

## Security

Any repository can name any hub in the `HUB_ADDRESS` of its
`codesonar.conf`. So before the server signs in to a hub with your
credentials, it asks you in a notification, unless it is the hub of the
**URL** setting or one you trusted before. "Sign in" is remembered;
otherwise the hub is used without signing in.

An analysis runs the build commands of `codesonar.conf`, just as `make`
runs a Makefile, so only analyze repositories you trust. The plugin starts
analyses only with **Analyze** and **Analyze As...**.

Links of analyses and the hub are only opened if they are web links.

## Actions

The actions are in **Tools | CodeSonar** and in the context menu of editors
with a running server. They have no shortcuts; assign some in **Settings |
Keymap**. The actions need a running server, so use them with a file of the
workspace open.

| Action                   | Description |
| ------------------------ | ----------- |
| Analyses... | List the analyses of the hub project, newest first. The shown one is marked with `*`, one that is loading with `~`, downloaded ones with `[cached]`. `Enter` or a double click shows an analysis, `Space` shows or hides one and keeps the list open, `x` deletes its local copy, `d` deletes it from the hub after a confirmation. |
| Load Configured Analysis | Show the configured analysis. |
| Load SARIF File... | Show an exported SARIF file. |
| Refresh | Load the shown analysis again. |
| Hide Analysis / Show Analysis | Turn the shown analysis off, or the hidden one on again. The menu shows what it does now; in **Settings \| Keymap** it is "Toggle Analysis". |
| Explain Warning | Explain the warnings in the line of the caret in a popup, like the "Show details" quick fix: the message, the events that lead to the warning and the rules it may violate. The places of events are links into the code. `j` `k` `Ctrl+D` `Ctrl+U` `g` `G` scroll, `o` opens the warning on the hub, `d` the documentation, `q` or `Esc` closes. |
| Open in Hub | Open the warning at the caret on the hub, or else the shown analysis. |
| Mark as Fixed | Resolve the warnings in the line of the caret on the hub, like the quick fix: their state becomes Fixed. The hub keeps it for later analyses, and resolved warnings are not shown any more. Your hub user needs the permission to change warnings. |
| Mark as False Positive | Resolve them as wrong: their finding becomes False Positive. |
| Disable Check | Turn off the check of the warning in the line of the caret, like the quick fix: the check is disabled in the `codesonar.conf` of the workspace for later analyses, and its warnings are hidden now. With several warnings in the line, a list asks for the check. **Checks...** turns it on again. |
| Show Warnings | List the shown warnings in the CodeSonar tool window. `Enter` or a double click goes to a warning. The list is updated when another analysis is shown. |
| Status | Show the analysis with its page on the hub, how many of its warnings are unchanged, moved, changed, unverified and resolved, and the hub and presets of new analyses. `o` opens the analysis on the hub. A click on the status bar widget shows it as well. |
| Analyze | Run `codesonar analyze` for the workspace in the Run tool window. When it succeeds, the new analysis is shown. |
| Analyze As... | Run an analysis with a name on the hub. |
| Workspace Configuration | Open the `codesonar.conf` of the workspace. Without it, or without the section of codesonar-lsp in it, a form asks for the basic settings first. |
| Presets... | Tick the CodeSonar presets of analyses, e.g. `misrac2025`. The field filters them while typing. |
| Checks... | Tick the checks (warning classes) of analyses. Checks that differ from CodeSonar's default are marked "(changed)". |
| Connect to Hub... | Sign in to a hub and choose one of its projects. Without a URL, the configured hub is used. The hub, the user and the password are kept in the settings, unless "Remember" is unticked. |
| Projects... | Choose another project of the hub. |

While an analysis is downloaded, the IDE shows the progress as a
background task. The status bar widget shows the analysis; it can be turned
off in the context menu of the status bar. Hub links open in the browser of
**Settings | Tools | Web Browsers and Preview**.

## Development

```sh
./gradlew test          # add -PclionPath=/opt/clion for an installed CLion
./gradlew runIde        # starts CLion with the plugin
```

The tests of the protocol run against `codesonar-lsp` in `$CODESONAR_LSP`,
or the one in `$PATH`, and are skipped without it:

```sh
CODESONAR_LSP=~/.cargo/bin/codesonar-lsp ./gradlew test
```

Other plugins can subscribe to `CodeSonarListener.TOPIC` on the message bus
of the project to learn when an analysis starts loading and when it is
shown.

To release a version, set `pluginVersion` in `gradle.properties`, commit,
and push a tag named after it:

```sh
git tag v0.1.0
git push origin v0.1.0
```

The release workflow then runs the tests, against the released
codesonar-lsp, builds the plugin and publishes its ZIP file as a GitHub
release with its checksum.

## License

[MIT](LICENSE)
