#!/bin/sh
echo "Launching start hook -> Create SSM Parameter"

parameter_name="/local/stcp-realtime-data-ingestor/secrets/hmac-secrets"

aws --endpoint-url=http://localhost:4566 ssm put-parameter \
      --name "$parameter_name" \
      --value '{
         "secret_1": {
            "secret": "LUnKmWPnlY+EHNeRmSSDM6s2Z4kZzs58K3dc0cNofSX0edt0TuE102XnahWa3t/seTcUmZGiICHdS86NKR72PA==",
            "createdAt": "2025-04-07T16:49:47Z"
         },
         "secret_2": {
            "secret": "nC5F3SdfXxnOi1Ux1/rieiyoYoHtKuaH+eo5q6MkCMph+d8jkwHNr6ArFbiSsnj6DS4UEpWIvd5bO6GtcOPotA==",
            "createdAt": "2025-04-08T16:49:47Z"
         }
      }' \
      --type "SecureString" \
      --key-id "alias/aws/ssm"

parameter_arn=$(aws --endpoint-url=http://localhost:4566 ssm get-parameter \
      --name "$parameter_name" \
      --query "Parameter.ARN")

echo "---------------------------------------------------"
echo "USE THE FOLLOWING ARN IN application-dev.properties"
echo "---------------------------------------------------"
echo $parameter_arn
