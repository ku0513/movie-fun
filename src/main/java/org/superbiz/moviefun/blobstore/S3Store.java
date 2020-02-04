package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class S3Store implements BlobStore {
    private final AmazonS3Client s3Client;
    private final String bucketName;

    public S3Store(AmazonS3Client s3Client, String bucket) {
        this.s3Client = s3Client;
        this.bucketName = bucket;
    }

    @Override
    public void put(Blob blob) throws IOException {
        s3Client.putObject(bucketName, blob.name, blob.inputStream, new ObjectMetadata());
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        if (!s3Client.doesObjectExist(bucketName, name)) {
            return Optional.empty();
        }

        try (S3Object s3object = s3Client.getObject(bucketName, name)) {
            S3ObjectInputStream content = s3object.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(content);
            InputStream inputStream = new ByteArrayInputStream(bytes);
            Blob blob = new Blob(s3object.getBucketName(),
                    inputStream,
                    s3object.getObjectMetadata().getContentType());
            return Optional.of(blob);
        }
    }

    @Override
    public void deleteAll() {
        List<S3ObjectSummary> summaries = s3Client.listObjects(bucketName).getObjectSummaries();

        for (S3ObjectSummary summary: summaries) {
            s3Client.deleteObject(bucketName, summary.getKey());
        }

    }
}
