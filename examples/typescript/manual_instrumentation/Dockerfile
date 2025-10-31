FROM node:alpine

RUN apk add --no-cache tini

WORKDIR /src/app

ENV PORT=5000

EXPOSE ${PORT}

COPY package*.json ./

RUN npm install && npm install -g typescript

COPY . ./

RUN tsc

ENTRYPOINT ["/sbin/tini","--"]

CMD ["node","-r","./dist/tracing.js","./dist/app.js"]


