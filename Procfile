web: export GENOME_NEXUS_IMPORT_COMMIT=7f3b1823d9cc418a697154cbbfbd996152f16c54 && IMPORT_DIR=$(bash <(curl -L "https://github.com/genome-nexus/genome-nexus-importer/blob/${GENOME_NEXUS_IMPORT_COMMIT}/scripts/download_files_from_github_url.sh?raw=true") https://github.com/genome-nexus/genome-nexus-importer/tree/${GENOME_NEXUS_IMPORT_COMMIT}/export) && sleep 1s && bash $IMPORT_DIR/scripts/import_mongo.sh ${MONGODB_URI} && rm -rf $IMPORT_DIR && SERVER_PORT=${PORT} java $JAVA_OPTS -Dspring.data.mongodb.uri=${MONGODB_URI} -jar web/target/web-*.jar
