yum install https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
curl -L https://setup.ius.io/ | bash
yum groupinstall 'Development Tools' -y
yum install net-tools wget htop zip unzip vim pcre pcre-devel zlib zlib-devel openssl openssl-devel -y

