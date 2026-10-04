# Workflow

Our group follows a common workflow when developing Dinnertracker. The
workflow is based on GitHub Issues, branches, pull requests and code
review. It is intended to make the development process predictable and
make it easier for several group members to work efficiently on the project at the
same time.


## 1. Identify and provide issues

A development task is first identified and described as a GitHub Issue.
The issue explains what needs to be done and, when relevant, and which
requirements it needs. 

The group uses an issue template when creating issues. The template
provides a common structure and helps make sure that important
information is included.

The issue is assigned to one or more group members and added to the
appropriate milestone. 


## 2. Create a branch

A separate branch is created for each issue and will be connected to its issue through the issue number. The branch is based on the
current `main` branch and is named according to the issue or task. 

Development is done in this branch rather than directly in `main`. This is done to minimize merge conflicts. 


## 3. Implement the changes

The assigned group member or pair works on the task in their own 
branch. The implementation is discussed and developed according to the
requirements described in the issue.

When pair programming is used, the two contributors work together on
the same task and discuss implementation decisions throughout the
session.


## 4. Test the changes

The implementation is tested before it is submitted for review.
Relevant automated tests are run to verify that the new functionality
works and that existing functionality has not been broken.

The project is also checked using the relevant Maven and code quality
tools. This includes `mvn verify`, `mvn config`, and `mvn test jacoco:report`. 


## 5. Commit and push

The changes are committed with descriptive commit messages including conventional commits, and pushed
to the remote repository.

For work completed through pair programming, the commit includes a
`Co-authored-by:` trailer for the second contributor.



## 6. Create a pull request

When the task is finished and pushed, a pull request is created from their remote
branch to `main`.

The group uses a pull request template to provide a common structure
for pull requests. This is generated automatically when creating a PR. The author describes the changes made and references
the GitHub Issue that the work belongs to by closing the issue.



## 7. Review

Another group member reviews the pull request. The reviewer checks the
implementation, tests and overall consistency with the project.

If changes are requested, they are discussed and implemented in the
remote branch before the pull request is merged.

The group can use Teams to discuss questions or feedback related to the
pull request during the review process. This can also be discussed in person in our meetings, or when working together. 



## 8. Merge and close the issue

When the pull request has been reviewed and the changes look good, it
is merged into `main`.

The corresponding GitHub Issue is then closed, either manually or
through the pull request if it contains the appropriate issue-closing
reference.

This workflow is repeated for the different development tasks
throughout the project.

