# Fix Unresolved reference 'clearPublishFeedback'

The `ExpertScreen.kt` calls `viewModel.clearPublishFeedback()`, but `ExpertViewModel` has it named as `clearPublishError()`. Additionally, `ExpertScreen.kt` expects `publishFeedbackRes` and `publishFeedbackIsError` in `ExpertUiState`, but `ExpertViewModel.kt` currently uses `publishErrorRes`.

## Proposed Changes

### [Component Name] Expert UI Logic

#### [MODIFY] [ExpertViewModel.kt](file:///C:/Users/user/AndroidStudioProjects/warmapp/app/src/main/java/com/example/myapplication/di/ExpertViewModel.kt)
- Update `ExpertUiState`:
    - Rename `publishErrorRes` to `publishFeedbackRes`.
    - Add `publishFeedbackIsError: Boolean = false`.
- Update `ExpertViewModel` methods:
    - Rename `clearPublishError()` to `clearPublishFeedback()`.
    - Update `clearPublishFeedback()` to reset both `publishFeedbackRes` and `publishFeedbackIsError`.
    - Update `publishSkill()` to:
        - Set `publishFeedbackRes` and `publishFeedbackIsError = true` when validation fails.
        - Set `publishFeedbackRes` and `publishFeedbackIsError = false` upon successful submission (optional but recommended since the UI supports success feedback).

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify the fix.

### Manual Verification
- Deploy the app and verify that the feedback card appears correctly for both validation errors and successful submissions.
- Verify that typing in the QuickLogCard clears the feedback.
