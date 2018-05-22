provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

data "vault_generic_secret" "sscs_s2s_secret" {
  path = "secret/${var.infrastructure_env}/ccidam/service-auth-provider/api/microservice-keys/sscs"
}

locals {
  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  previewVaultName       = "${var.product}-${var.component}"
  nonPreviewVaultName    = "${var.product}-${var.component}-${var.env}"
  vaultName              = "${(var.env == "preview") ? local.previewVaultName : local.nonPreviewVaultName}"
  
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "core-compute-aat" : "core-compute-saat" : local.aseName}"

  ccdApi = "http://ccd-data-store-api-${local.local_env}.service.${local.local_ase}.internal"
  s2sCnpUrl = "http://rpe-service-auth-provider-${local.local_env}.service.${local.local_ase}.internal"
}

module "sscs-job-scheduler" {
  source              = "git@github.com:hmcts/moj-module-webapp.git?ref=master"
  product             = "${var.product}-${var.component}"
  location            = "${var.location}"
  env                 = "${var.env}"
  ilbIp               = "${var.ilbIp}"
  is_frontend         = false
  subscription        = "${var.subscription}"

  app_settings = {
    // logging vars
    REFORM_TEAM         = "${var.product}"
    REFORM_SERVICE_NAME = "${var.component}"
    REFORM_ENVIRONMENT  = "${var.env}"

    // IdAM s2s
    S2S_URL = "${local.s2sCnpUrl}"
    S2S_SECRET = "${data.vault_generic_secret.s2s_secret.data["value"]}"
    S2S_MICROSERVICE = "${var.idam_s2s_auth_microservice}"

    // db vars
    # JOB_SCHEDULER_DB_HOST     = "${module.job-scheduler-database.host_name}"
    # JOB_SCHEDULER_DB_PORT     = "${module.job-scheduler-database.postgresql_listen_port}"
    # JOB_SCHEDULER_DB_PASSWORD = "${module.job-scheduler-database.postgresql_password}"
    # JOB_SCHEDULER_DB_USERNAME = "${module.job-scheduler-database.user_name}"
    JOB_SCHEDULER_DB_NAME     = "postgres"
    JOB_SCHEDULER_DB_CONNECTION_OPTIONS = "?ssl"
  }
}

module "job-scheduler-vault" {
  source                  = "git@github.com:contino/moj-module-key-vault?ref=master"
  name                    = "${var.product}-${var.microservice}-${var.env}"
  product                 = "${var.product}"
  env                     = "${var.env}"
  tenant_id               = "${var.tenant_id}"
  object_id               = "${var.jenkins_AAD_objectId}"
  resource_group_name     = "${module.sscs-job-scheduler-api.resource_group_name}"
  product_group_object_id = "38f9dea6-e861-4a50-9e73-21e64f563537"
}

# resource "azurerm_key_vault_secret" "POSTGRES-USER" {
#   name      = "job-scheduler-POSTGRES-USER"
#   value     = "${module.job-scheduler-database.user_name}"
#   vault_uri = "${module.job-scheduler-vault.key_vault_uri}"
# }

# resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
#   name      = "job-scheduler-POSTGRES-PASS"
#   value     = "${module.job-scheduler-database.postgresql_password}"
#   vault_uri = "${module.job-scheduler-vault.key_vault_uri}"
# }

# resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
#   name      = "job-scheduler-POSTGRES-HOST"
#   value     = "${module.job-scheduler-database.host_name}"
#   vault_uri = "${module.job-scheduler-vault.key_vault_uri}"
# }

# resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
#   name      = "job-scheduler-POSTGRES-PORT"
#   value     = "${module.job-scheduler-database.postgresql_listen_port}"
#   vault_uri = "${module.job-scheduler-vault.key_vault_uri}"
# }

# resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
#   name      = "job-scheduler-POSTGRES-DATABASE"
#   value     = "${module.job-scheduler-database.postgresql_database}"
#   vault_uri = "${module.job-scheduler-vault.key_vault_uri}"
# }
