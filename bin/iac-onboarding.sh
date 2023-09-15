#!/bin/bash
# Error handling - exit on any error
   set -e
# update ubuntu before installing apps
sudo apt update
#shell script for configuring and running docker using wsl2
# Check if AzureCLI is installed
if [ -x "$(command -v az)" ]; then
    echo "AzureCLI is already installed"
    else
    echo "Installing AzureCLI"
    curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash
fi
#Install Node.js
# Check if Node.js is installed
if [ -x "$(command -v node)" ]; then
    echo "Node.js is already installed"
    else
    echo "Installing Node.js"
    sudo apt-get install -y nodejs
fi
#Install yarn
# Check if yarn is installed
if [ -x "$(command -v yarn)" ]; then
    echo "yarn is already installed"
    else
    echo "Installing yarn"
    sudo npm install -g yarn
fi
#echo yarn version
yarn --version
#Install JQuery
# Check if JQuery is installed
if [ -x "$(command -v jquery)" ]; then
    echo "JQuery is already installed"
    else
    echo "Installing JQuery"
    sudo npm install jquery
fi

#Install JQ
# Check if JQ is installed
if [ -x "$(command -v jq)" ]; then
    echo "JQ is already installed"
    else
    echo "Installing JQ"
    sudo apt-get install -y jq
fi

# Check if user has an id_rsa or id_ed25519 key
if [ -f "$HOME/.ssh/id_rsa" ] || [ -f "$HOME/.ssh/id_ed25519" ]; then
  echo "SSH key found - Please make sure this is added to github and authorised for SSO"
else
  echo "No SSH key found - Please generate a new key, add it to github and authorise SSO on it"
  echo "From the github docs (https://docs.github.com/en/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent)"
  echo "> ssh-keygen -t ed25519 -C "your_email@example.com""
  echo "> eval \"\$(ssh-agent -s)\""
  echo "> ssh-add ~/.ssh/id_ed25519"
  echo "> cat ~/.ssh/id_ed25519.pub"
  echo "Copy the output of the last command and add it to github > settings > SSH and GPG keys > New SSH key > Configure SSO > hmcts > Authorise"
  exit 0
fi

# Clone repos first because it likely has a user prompt with fingerprint
REPOS=(
  "ia-docker"
  "ia-ccd-definitions"
)

for repo in "${REPOS[@]}"; do
  if [ ! -d "$HOME/$repo" ]; then
    git clone "git@github.com:hmcts/$repo.git" "$HOME/$repo"
  fi
done
