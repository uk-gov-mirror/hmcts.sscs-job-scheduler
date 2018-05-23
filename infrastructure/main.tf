provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

locals {
  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "core-compute-aat" : "core-compute-saat" : local.aseName}"

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
  }
}
