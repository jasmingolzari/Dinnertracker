# Work Practices

Our group uses several development practices and tools to organize the
work and maintain the quality of the Dinnertracker project. These
practices are based on the requirements of the project and on our
experiences from Release 1.
 
## GitHub Issues and Issue Templates
 
GitHub Issues are used to divide the project into smaller development
tasks. Each issue describes what needs to be done and can be assigned to
one or more group members. Issues are also used to keep track of the
progress of the different tasks.
 
The group uses an issue template to provide a consistent structure for
new issues. The template helps us include relevant information when
describing a task and makes it easier for other group members to
understand what needs to be done.
 
The issues are connected to milestones so that the group can organize
work according to the different releases. We distribute the issues on our first meeting for the release we're working on.
 
 
## Branches
 
Development work is done in separate branches instead of directly on
the `main` branch. A branch is created for a specific issue and is used
while implementing and testing the changes related to that issue.
 
This makes it possible for several group members to work on different
tasks at the same time without directly affecting the main branch.
 
## Pull Requests and Code Review
 
When a task is completed, a pull request is created to merge the branch
into `main`. The pull request describes the changes and is connected to
the relevant issue.
 
The group uses a pull request template to provide a consistent
structure for pull requests. This helps the author describe what has
been changed and makes it easier for reviewers to understand what they
should look at.
 
Other group members review the changes before they are merged. This
gives the group an opportunity to find problems, discuss implementation
choices and make sure that the changes fit with the rest of the project.
 
## Milestones
 
Milestones are used to organize issues according to the different
releases. This gives the group an overview of which tasks belong to a
specific release and makes it easier to keep track of the progress
towards the release requirements.
 
## Pair Programming
 
Pair programming is used for selected development tasks. Two group
members work together on the same task and discuss the implementation
while working.
 
Pair programming is also intended to improve knowledge sharing within
the group, since both contributors are involved in understanding the
implementation.
 
For pair-programmed work, both contributors are credited in the commit
using a `Co-authored-by:` trailer.
 
## Commit Messages
 
The group uses descriptive commit messages to make it clear what each
commit changes. We use a consistent format for commits where possible,
such as Conventional Commits.
 
For example:
 
```text
docs: update application README
fix: handle empty ingredient list
feat: add fridge functionality
```
 
For pair-programmed commits, a Co-authored-by: trailer is included to
credit both contributors.
 
 
## Communication
 
The group uses Microsoft Teams as the main communication channel
between meetings. The chat is used for both organizational and technical
communication.
 
We use Teams to coordinate meetings, share updates about what we are
working on, ask questions, discuss problems and give each other
feedback. Group members also use the chat to let others know when a
task has been completed and ask for someone to take a look at the
changes.
 
This gives the group a continuous way of communicating during
development instead of relying only on scheduled meetings.
 
