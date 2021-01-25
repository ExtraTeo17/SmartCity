FROM openjdk:14-oraclelinux7

# install nodejs
RUN yum install -y curl
ENV NODE_VERSION=14.15.1
RUN curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.34.0/install.sh | bash
ENV NVM_DIR=/root/.nvm
RUN . "$NVM_DIR/nvm.sh" && nvm install ${NODE_VERSION}
RUN . "$NVM_DIR/nvm.sh" && nvm use v${NODE_VERSION}
RUN . "$NVM_DIR/nvm.sh" && nvm alias default v${NODE_VERSION}
ENV PATH="/root/.nvm/versions/node/v${NODE_VERSION}/bin/:${PATH}"
RUN node --version && npm --version
RUN npm install serve -g

# copy SmartCity-build
COPY SmartCity-build ./SmartCity

# automatically start app
CMD ["sh","-c","cd SmartCity && ./run.sh"]