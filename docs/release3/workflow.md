# Workflow

Our group follows a common workflow when developing Dinnertracker. The
workflow is based on GitHub Issues, branches, pull requests and code
review. Its purpose is to make the development process predictable and
to make it easier for several group members to work on the project at
the same time.

The process in release 3 is mostly the same as in release 2, but we have
been more thorough: issues and pull requests follow our templates, every
pull request is reviewed, and the automatic checks must pass before
merging. We have also used more milestones, which played a bigger role in
how we planned and prioritised our work.

## 1. Create an issue

A development task is first identified and described as a GitHub Issue.
The issue explains what needs to be done and, when relevant, which
requirements it must meet.

The group uses an issue template when creating issues. The template
provides a common structure with a description, tasks, acceptance
criteria and additional context, and helps make sure that important
information is included.

The issue is assigned to one or more group members.

## 2. Add a milestone

After an issue has been assigned, we add it to an appropriate milestone.
In release 3, we created more internal milestones to set our own
deadlines before the final release 3 deadline. These include a
development deadline, a documentation milestone and a hard deadline.

This helped us finish the code first, leave time for documentation, and
avoid last-minute work before the delivery.

## 3. Create a branch

A separate branch is created for each issue. The branch is based on the
current `main` branch and is named with the issue number and a short
description, for example `218-implement-rest-client-and-change-ui`. This
connects the branch to its issue.

All development is done in this branch rather than directly in `main`.
This keeps `main` stable and makes sure that every change is reviewed
before it is merged.

## 4. Implement the changes

The assigned group member or pair works on the task in their own branch.
The implementation is discussed and developed according to the
requirements described in the issue.

When pair programming is used, the two contributors work together on the
same task and discuss implementation decisions throughout the session.

## 5. Test the changes

The implementation is tested before it is submitted for review. Relevant
automated tests are added or updated to verify that the new functionality
works and that existing functionality has not been broken.

Before pushing, we run `mvn spotless:apply` to format the code and
`mvn verify` to run all tests together with Checkstyle, SpotBugs,
Spotless and the JaCoCo coverage check.

## 6. Commit and push

The changes are committed with descriptive commit messages following the
Conventional Commits format, such as `feat:`, `fix:`, `test:` and
`docs:`, and pushed to the remote repository.

For work completed through pair programming, the commit includes a
`Co-authored-by:` trailer for the second contributor.

## 7. Create a pull request

When the task is finished and pushed, a pull request is created from the
issue branch to `main`.

The group uses a pull request template, which is added automatically when
a pull request is created. The author describes the changes and how they
were tested, and links the pull request to its issue with
`Closes #<issue-number>`.

## 8. Review

When a pull request is opened or updated, an automatic GitHub Actions
pipeline builds the project and runs all tests and code quality checks.

Another group member then reviews the pull request. The reviewer checks
the implementation, the tests and the overall consistency with the
project. If changes are requested, they are discussed and made in the
same branch before the pull request is merged.

The group also uses Teams to discuss questions or feedback during the
review, and we discuss larger questions in person at our meetings or
when working together.

## 9. Merge and close the issue

When the pull request has been approved and all automatic checks pass,
it is merged into `main`. The branch is then deleted.

The corresponding GitHub Issue is closed automatically through the
`Closes #<issue-number>` reference, or manually if the reference is
missing.

This workflow is repeated for each development task throughout the
project.