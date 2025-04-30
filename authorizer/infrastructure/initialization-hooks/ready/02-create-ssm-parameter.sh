#!/bin/sh
echo "Launching start hook -> Create SSM Parameter"

parameter_name_prefix="/local/stcp-realtime-data-ingestor/secrets"

aws --endpoint-url=http://localhost:4566 ssm put-parameter \
      --name "$parameter_name_prefix/secret_1" \
      --value "LUnKmWPnlY+EHNeRmSSDM6s2Z4kZzs58K3dc0cNofSX0edt0TuE102XnahWa3t/seTcUmZGiICHdS86NKR72PA==" \
      --type "SecureString" \
      --key-id "alias/aws/ssm"

aws --endpoint-url=http://localhost:4566 ssm put-parameter \
      --name "$parameter_name_prefix/secret_2" \
      --value "nC5F3SdfXxnOi1Ux1/rieiyoYoHtKuaH+eo5q6MkCMph+d8jkwHNr6ArFbiSsnj6DS4UEpWIvd5bO6GtcOPotA==" \
      --type "SecureString" \
      --key-id "alias/aws/ssm"
