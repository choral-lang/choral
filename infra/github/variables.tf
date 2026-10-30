variable "repositories" {
  description = "Public choral-lang repositories whose default branches require pull requests and squash merges."
  type        = set(string)
  default     = ["choral"]

  validation {
    condition     = contains(var.repositories, "choral")
    error_message = "Include choral, whose existing ruleset is adopted by the import block."
  }
}
