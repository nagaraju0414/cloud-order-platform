resource "google_container_cluster" "cloud_order_gke" {
  name     = "${var.project_name}-gke"
  location = var.region

  network    = google_compute_network.cloud_order_vpc.id
  subnetwork = google_compute_subnetwork.cloud_order_subnet.id

  deletion_protection = false

  remove_default_node_pool = true
  initial_node_count       = 1

  networking_mode = "VPC_NATIVE"

  ip_allocation_policy {
    cluster_secondary_range_name  = "gke-pod-range"
    services_secondary_range_name = "gke-service-range"
  }
}
resource "google_container_node_pool" "cloud_order_nodes" {
  name       = "${var.project_name}-node-pool"
  location   = var.region
  cluster    = google_container_cluster.cloud_order_gke.name
  node_count = 1

  node_config {
    machine_type = "e2-medium"

    disk_type    = "pd-standard"
    disk_size_gb = 30

    oauth_scopes = [
      "https://www.googleapis.com/auth/cloud-platform"
    ]

    labels = {
      environment = var.environment
      project     = var.project_name
    }

    tags = [
      "cloud-order-gke"
    ]
  }

  management {
    auto_repair  = true
    auto_upgrade = true
  }
}