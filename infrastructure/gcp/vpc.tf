resource "google_compute_network" "cloud_order_vpc" {
  name                    = "${var.project_name}-vpc"
  auto_create_subnetworks = false
  description             = "VPC for Cloud Order Platform"
}

resource "google_compute_subnetwork" "cloud_order_subnet" {
  name          = "${var.project_name}-subnet"
  ip_cidr_range = "10.10.0.0/20"
  region        = var.region
  network       = google_compute_network.cloud_order_vpc.id

  secondary_ip_range {
    range_name    = "gke-pod-range"
    ip_cidr_range = "10.20.0.0/16"
  }

  secondary_ip_range {
    range_name    = "gke-service-range"
    ip_cidr_range = "10.30.0.0/20"
  }

  private_ip_google_access = true
}