provider "github" {
  owner = "choral-lang"
}

resource "github_repository_ruleset" "default_branch" {
  for_each = var.repositories

  repository  = each.value
  name        = "Default branch protection"
  target      = "branch"
  enforcement = "active"

  conditions {
    ref_name {
      include = ["~DEFAULT_BRANCH"]
      exclude = []
    }
  }

  rules {
    deletion                = true
    non_fast_forward        = true
    required_linear_history = true

    pull_request {
      allowed_merge_methods             = ["squash"]
      required_approving_review_count   = 0
      require_code_owner_review         = false
      require_last_push_approval        = false
      dismiss_stale_reviews_on_push     = false
      required_review_thread_resolution = false
    }
  }

  # No bypass actors: repository administrators must also use pull requests.
}

# Adopt Choral's existing ruleset rather than creating a second one.
import {
  to = github_repository_ruleset.default_branch["choral"]
  id = "choral:22714427"
}
