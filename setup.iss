[Setup]
AppName=Cashflow Dashboard
AppVersion=1.0
AppPublisher=Your Company
AppPublisherURL=https://your-website.com
AppSupportURL=https://your-website.com/support
AppUpdatesURL=https://your-website.com/updates
DefaultDirName={autopf}\Cashflow Dashboard
DefaultGroupName=Cashflow Dashboard
AllowNoIcons=yes
OutputDir=installer
OutputBaseFilename=CashflowDashboard-Setup
Compression=lzma
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=lowest
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64
SetupIconFile=icon\icon.ico

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked; OnlyBelowVersion: 6.1; Check: not IsAdminInstallMode

[Files]
Source: "target\cashflow-dashboard-1.0-SNAPSHOT.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "run-app.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "README.md"; DestDir: "{app}"; Flags: ignoreversion
Source: "icon\icon.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "jdk\*"; DestDir: "{app}\jdk"; Flags: recursesubdirs ignoreversion

[Icons]
Name: "{group}\Cashflow Dashboard"; Filename: "{app}\run-app.bat"; WorkingDir: "{app}"; IconFilename: "{app}\icon.ico"
Name: "{group}\{cm:UninstallProgram,Cashflow Dashboard}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\Cashflow Dashboard"; Filename: "{app}\run-app.bat"; WorkingDir: "{app}"; IconFilename: "{app}\icon.ico"; Tasks: desktopicon
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\Cashflow Dashboard"; Filename: "{app}\run-app.bat"; WorkingDir: "{app}"; IconFilename: "{app}\icon.ico"; Tasks: quicklaunchicon

[Run]
Filename: "{app}\run-app.bat"; Description: "{cm:LaunchProgram,Cashflow Dashboard}"; Flags: nowait postinstall skipifsilent