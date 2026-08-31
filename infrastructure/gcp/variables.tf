variable "project_id" {
  description = "GCP project ID"
  type        = string
}

variable "project_name" {
  description = "Application/project name"
  type        = string
  default     = "cloud-order-platform"
}

variable "region" {
  description = "GCP region"
  type        = string
  default     = "asia-south1"
}

variable "zone" {
  description = "GCP zone"
  type        = string
  default     = "asia-south1-a"
}
variable "environment" {
  description = "Deployment environment"
  type        = string
  default     = "dev"
}