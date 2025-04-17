# Use AWS Lambda Node.js base image
FROM public.ecr.aws/lambda/nodejs:20

WORKDIR /var/task

# Copy package.json files
COPY package*.json ./

# Copy the .npmrc file - simple relative path
COPY .npmrc .npmrc

RUN npm install

# Copy the rest of the application code
COPY . .

# Set the Lambda function entry point
CMD ["node", "lambda.js"]