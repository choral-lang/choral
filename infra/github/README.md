# GitHub repository rules

This Terraform configuration manages repository-level branch rulesets for public
repositories in `choral-lang`. These rulesets are available on GitHub Free; they
do not require a paid organization-wide ruleset.

The default target is `choral`. Each managed repository's default branch requires
a pull request and allows only squash merges. No approving review is required,
so maintainers can merge their own PRs. Force pushes and deletion remain blocked,
and there are no bypass actors, including administrators. Repository admins can
still deliberately edit or disable a repository-level ruleset.

## Initialize and review

Use Terraform 1.5 or newer and authenticate with `gh auth login`, or supply a
`GITHUB_TOKEN` with repository Administration write permission. Credentials stay
outside Terraform configuration. Avoid setting `GITHUB_OWNER` or
`GITHUB_ORGANIZATION`, which can override the configured owner in this provider.

```sh
cd infra/github
terraform init
terraform fmt -check
terraform validate
terraform plan -out=rules.tfplan
```

The import block adopts Choral's existing ruleset, ID `22714427`. The initial plan
should import that ruleset and add its pull-request rule in place, preserving the
existing force-push, deletion, and linear-history protections. Check that there
are no unexpected replacements or deletions.

Creating or merging this configuration PR does not change GitHub settings.
After reviewing the saved plan, apply it explicitly:

```sh
terraform apply rules.tfplan
```

Subsequent plans should report no changes unless the configuration or live
settings change. The provider lock file is committed; provider downloads, state,
saved plans, and local variable files are ignored. Keep the local state and its
backup securely and use the same state for later runs. Before multiple maintainers
operate this configuration, configure a shared backend with state locking.

## Add repositories

Set `repositories` in an ignored `terraform.tfvars` file, always including
`choral`, then review and apply the plan:

```hcl
repositories = ["choral", "another-public-repository"]
```

For another repository with an existing ruleset, import it before applying, or
add an import block like Choral's. For example:

```sh
terraform import 'github_repository_ruleset.default_branch["another-public-repository"]' 'another-public-repository:RULESET_ID'
```

Ensure squash merging is enabled in each repository's General settings. Existing
repository rulesets apply alongside these rules. New repositories are protected
only after they are added and this configuration is applied; repository admins
can change their local rulesets. Removing a repository from this set plans to
delete its managed ruleset. Private organization repositories require an eligible
paid GitHub plan for these protections.

## References

- [GitHub ruleset availability](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/about-rulesets)
- [Terraform GitHub repository ruleset resource](https://registry.terraform.io/providers/integrations/github/latest/docs/resources/repository_ruleset)
- [GitHub provider authentication](https://registry.terraform.io/providers/integrations/github/latest/docs)
