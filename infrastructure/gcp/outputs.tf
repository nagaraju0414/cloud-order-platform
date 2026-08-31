output "vpc_name" {
  value = google_compute_network.cloud_order_vpc.name
}

output "vpc_id" {
  value = google_compute_network.cloud_order_vpc.id
}

output "subnet_name" {
  value = google_compute_subnetwork.cloud_order_subnet.name
}

output "subnet_cidr" {
  value = google_compute_subnetwork.cloud_order_subnet.ip_cidr_range
}

output "pod_range" {
  value = "10.20.0.0/16"
}

output "service_range" {
  value = "10.30.0.0/20"
}
output "gke_cluster_name" {
  value = google_container_cluster.cloud_order_gke.name
}

output "gke_cluster_location" {
  value = google_container_cluster.cloud_order_gke.location
}

output "gke_endpoint" {
  value     = google_container_cluster.cloud_order_gke.endpoint
  sensitive = true
}