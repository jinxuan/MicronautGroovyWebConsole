package io.github.joxebus.groovywebconsole.service.common.impl

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.github.joxebus.groovywebconsole.pojo.FileResponse
import io.github.joxebus.groovywebconsole.service.common.FileService
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.http.server.types.files.SystemFile

import javax.annotation.PostConstruct
import javax.inject.Singleton

@Singleton
@CompileStatic
@Requires(env = "local")
@Slf4j
class FileServiceImpl implements FileService {

    @Value('${file.upload.folder}')
    private String fileUploadFolder
    @Value('${micronaut.server.baseUrl}')
    private String baseUrl

    @PostConstruct
    private final void init() {
        File file = new File(fileUploadFolder)
        log.info("Using folder [$fileUploadFolder] to upload files")
        if(!file.exists()) {
            log.warn("file folder not found, trying to create new folder")
            if(!file.mkdirs()) {
                log.error("file folder cannot be created")
                System.exit(1)
            }
            log.info("file folder created in location: [$fileUploadFolder]")
        }
    }

    @Override
    FileResponse upload(SystemFile file) {
        FileResponse fileResponse = new FileResponse()
        try {
            if(!file || !file.getFile()) {
                throw new RuntimeException("Failed to create empty file");
            }

            String filename = UUID.randomUUID().toString()
            File destination = new File(fileUploadFolder, filename)
            FileOutputStream fos = new FileOutputStream(destination)
            fos.write(file.getFile().getBytes())
            fos.close()

            fileResponse.uploaded = true
            fileResponse.url = "${baseUrl}/${filename}"
            log.info("File [${filename}] successfully saved")
        } catch(Exception e) {
            fileResponse.uploaded = false
            fileResponse.error = FileResponse.newError(e.getMessage())
            log.error("file cannot be uploaded", e)
        }
        fileResponse
    }


    byte[] download(String name) {
        try {
            File downloaded = new File(fileUploadFolder, name)
            if(!downloaded.exists()) {
                throw new RuntimeException("File [${name}] doesn't exist")
            }
            downloaded.bytes
        } catch(Exception e) {
            log.error('The file cannot be downloaded', e)
            null
        }
    }

}
