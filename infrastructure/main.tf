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
    S2S_SECRET = "${data.vault_generic_secret.sscs_s2s_secret.data["value"]}"
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
