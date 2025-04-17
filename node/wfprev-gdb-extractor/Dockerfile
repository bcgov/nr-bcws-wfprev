# Use AWS Lambda Node.js base image
FROM public.ecr.aws/lambda/nodejs:20

WORKDIR /var/task

COPY package*.json ./
COPY .npmrc .npmrc
RUN npm install

COPY . .

CMD ["node", "lambda.js"]
