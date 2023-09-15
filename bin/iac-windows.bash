#Creaing power shell for program installation and configuration

# Get the username of the currently logged-in user
$currentUserName = $env:USERNAME
# Set the path to the installer
$downloadPath = "C:\Users\$currentUserName\Downloads\ideaIU-2021.2.3.exe"
# Download IntelliJ IDEA Ultimate Edition
echo "Downloading IntelliJ IDEA Ultimate Edition"
Invoke-WebRequest -Uri "https://download.jetbrains.com/idea/ideaIU-2021.2.3.exe" -OutFile $downloadPath
# Run the installer
echo "Installing IntelliJ IDEA Ultimate Edition"
Start-Process -FilePath $downloadPath -ArgumentList "/S" -Wait
# Remove the installer
echo "Removing the installer"
Remove-Item $downloadPath

#download and install Dbeaver
$downloadPath = "C:\Users\$currentUserName\Downloads\dbeaver-ce-21.2.0-x86_64-setup.exe"
echo "Downloading Dbeaver"
Invoke-WebRequest -Uri "https://dbeaver.io/files/dbeaver-ce-latest-x86_64-setup.exe" -OutFile $downloadPath
echo "Installing Dbeaver"
Start-Process -FilePath $downloadPath -ArgumentList "/S" -Wait
echo "Removing the installer"
Remove-Item $downloadPath

#Chech if java is already installed and if not install java version 17  and set the path
$javaPath = "C:\Program Files\Java\jdk-17"
if (Test-Path $javaPath) {
    echo "Java is already installed"
} else {
    echo "Java is not installed"
    $downloadPath = "C:\Users\$currentUserName\Downloads\jdk-17_windows-x64_bin.exe"
    echo "Downloading Java"
    Invoke-WebRequest -Uri "https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.exe" -OutFile $downloadPath
    echo "Installing Java"
    Start-Process -FilePath $downloadPath -ArgumentList "/s" -Wait
    echo "Removing the installer"
    Remove-Item $downloadPath
    echo "Setting the path"
    $env:Path += ";C:\Program Files\Java\jdk-17\bin"
}

#Create wsl configuration file under C:\Users\username\.wslconfig and set the memory to 12GB
$wslConfigPath = "C:\Users\$currentUserName\.wslconfig"
if (Test-Path $wslConfigPath) {
    echo "wslconfig file already exists"
} else {
    echo "Creating wslconfig file"
    New-Item -Path $wslConfigPath -ItemType File
    echo "Setting the memory to 12GB"
    Add-Content -Path $wslConfigPath -Value "[wsl2]`nmemory=12GB"
}

