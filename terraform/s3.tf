# Bucket create. Public-read or private?
resource "aws_s3_bucket" "wfprev_site_bucket" {
  bucket        = "wfnews-${var.TARGET_ENV}-site"
  acl           = "public-read"
  force_destroy = true

  website {
    index_document = "index.html"
    error_document = "index.html"
  }
}

# Uploading assets. This shouldn't be needed because we'll push them up from the 
# github action, vs having terraform fetch them
#resource "aws_s3_object" "upload-assets" {
#  for_each = fileset("${var.web-assets-path}", "**/*")
#  bucket = aws_s3_bucket.wfprev_site_bucket.bucket
#  key = each.value
#  source = "${var.web-assets-path}/${each.value}"
#  content_type = lookup(var.mime_types, regex("\\.[^.]+$", each.value), "application/octet-stream")
#}

# S3 Bucket Policy for public access
resource "aws_s3_bucket_policy" "wfprev_site_bucket_policy" {
  bucket = aws_s3_bucket.wfprev_site_bucket.id

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "${aws_s3_bucket.wfprev_site_bucket.arn}/*"
      }
    ]
  })
}

output "s3_bucket_name" {
  value = aws_s3_bucket.wfprev_site_bucket.bucket
}
