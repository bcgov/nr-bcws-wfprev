#!/bin/bash

# Exit on any error
set -e

# Check for db/.env.local and create if it doesn't exist
if [ ! -f db/.env.local ]; then
    echo "⚠️  db/.env.local not found. Creating with default values..."
    cat > db/.env.local << EOF
WFPREV_DB_PASSWORD=password
POSTGRES_PASSWORD=password
EOF
    echo "Created db/.env.local with default passwords"
fi

echo "🐘 Setting up PostgreSQL..."
docker pull postgis/postgis:16-3.4

# Stop and remove existing container if it exists
docker rm -f wfprev-postgres 2>/dev/null || true

# Create a Docker network if it doesn't exist
docker network create wfprev-network 2>/dev/null || true

# Start PostgreSQL with database creation
docker run --name wfprev-postgres \
    --network wfprev-network \
    --env-file db/.env.local \
    -e POSTGRES_USER=wfprev \
    -e POSTGRES_DB=wfprev \
    -e POSTGRES_HOST_AUTH_METHOD=trust \
    -p 5432:5432 \
    -d postgis/postgis:16-3.4

echo "⏳ Waiting for PostgreSQL to be ready..."
# More comprehensive health check
max_attempts=30
attempt=1
while [ $attempt -le $max_attempts ]; do
    if docker exec wfprev-postgres pg_isready -U wfprev; then
        # Try to actually connect and run a query
        if docker exec wfprev-postgres psql -U wfprev -d wfprev -c '\l' >/dev/null 2>&1; then
            echo "✅ PostgreSQL is ready and accepting connections!"
            break
        fi
    fi

    if [ $attempt -eq $max_attempts ]; then
        echo "❌ Failed to connect to PostgreSQL after $max_attempts attempts"
        echo "🔍 Checking PostgreSQL logs:"
        docker logs wfprev-postgres
        exit 1
    fi

    echo "PostgreSQL is unavailable - attempt $attempt/$max_attempts - sleeping 2s"
    sleep 2
    attempt=$((attempt + 1))
done

echo "🏗️  Building Liquibase image..."
docker build -t liquibase -f db/Dockerfile.liquibase.local ./db

echo "📦 Running database migrations..."
docker run --rm \
    --network wfprev-network \
    --env-file db/.env.local \
    -v $(pwd)/db:/liquibase/workspace \
    liquibase \
    /bin/bash -c 'cd /liquibase/workspace && liquibase \
    --url=jdbc:postgresql://wfprev-postgres:5432/wfprev \
    --changelog-file=main-changelog.json \
    --username=wfprev \
    --password=$WFPREV_DB_PASSWORD \
    --logFile=/liquibase/workspace/liquibase.log \
    update'

# Clean up log file if successful
rm -f db/liquibase.log

echo "✅ Database setup complete!"