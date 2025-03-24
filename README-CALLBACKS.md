# Callback System in IA Case API

## Overview

The Immigration & Asylum Case API implements a callback system to handle various stages of case processing. This document explains how the callback system works, focusing on:

1. **Pre-Submit Callbacks**: Executed before a case is submitted/updated
2. **Post-Submit Callbacks**: Executed after a case is successfully submitted/updated

## Callback Flow
These are four types of callbacks we can configure on an event in CCD. All are optional and only configured based on the requirements.

```
 ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
 │                 │     │                 │     │                 │     │                 │
 │  About to Start │────▶│    Mid Event    │────▶│ About to Submit │────▶│    Submitted    │
 │                 │     │                 │     │                 │     │                 │
 └─────────────────┘     └─────────────────┘     └─────────────────┘     └─────────────────┘
     Pre-Submit             Pre-Submit             Pre-Submit             Post-Submit
```

## Pre-Submit Callbacks

Pre-submit callbacks are triggered before a case is submitted or updated. They serve two main purposes:

1. **Validation**: Ensure the case data is valid before proceeding
2. **Transformation**: Modify the case data as needed before submission

### Types of Pre-Submit Callbacks

1. **About to Start**: Triggered when a user is about to start a specific event
2. **Mid Event**: Triggered during the event, typically after a page is completed but before the event is submitted
3. **About to Submit**: Triggered when a user is about to submit an event

### Components Involved

- **PreSubmitCallbackController**: REST controller that receives pre-submit callback requests
- **PreSubmitCallbackDispatcher**: Dispatches callback requests to appropriate handlers
- **PreSubmitCallbackHandler**: Interface implemented by handlers that process pre-submit callbacks
- **PreSubmitCallbackStateHandler**: Special type of handler that can change the state of a case

## Post-Submit Callbacks

Post-submit callbacks are triggered after a case has been successfully submitted or updated. They are typically used for:

1. **Notifications**: Sending emails, texts, or other notifications
2. **Integration**: Triggering processes in other systems
3. **Audit**: Recording information about the completed action

### Components Involved

- **PostSubmitCallbackController**: REST controller that receives post-submit callback requests
- **PostSubmitCallbackDispatcher**: Dispatches callback requests to appropriate handlers
- **PostSubmitCallbackHandler**: Interface implemented by handlers that process post-submit callbacks

## Implementation Details

### PreSubmitCallbackController

This controller exposes endpoints for "about to start", "mid event", and "about to submit" callbacks. It:
- Receives callback requests with case data
- Delegates to the PreSubmitCallbackDispatcher
- Returns the (potentially modified) case data and any validation errors

### PostSubmitCallbackController

This controller exposes an endpoint for "submitted" callbacks. It:
- Receives callback requests with case data
- Delegates to the PostSubmitCallbackDispatcher
- Returns confirmation messages or other post-submission data

### Callback Handlers

Handlers implement either the PreSubmitCallbackHandler or PostSubmitCallbackHandler interface. Each handler:
- Declares which event types and states it can handle
- Contains business logic for processing the callback
- May modify the case data (pre-submit) or generate confirmation messages (post-submit)

## Example: AddAppealResponseHandler

The `AddAppealResponseHandler` is an example of a PreSubmitCallbackHandler that:
1. Handles the "addAppealResponse" event at the ABOUT_TO_SUBMIT stage
2. Validates that required fields are present
3. Updates case data with appeal response information
4. May change the state of the case

## Example: AgeAssessmentVisibilityHandler

The `AgeAssessmentVisibilityHandler` is an example of a PreSubmitCallbackHandler that:
1. Handles the "startAppeal" and "editAppeal" events at the MID_EVENT stage
2. Checks if the appeal is a non-detained appeal or a non-accelerated detained appeal
3. Sets the visibility of age assessment fields based on these conditions
4. Demonstrates how MID_EVENT callbacks can be used to control field visibility during form completion

## Callback Registration

Handlers are automatically discovered and registered through Spring's component scanning. To create a new handler:

1. Implement the appropriate interface (PreSubmitCallbackHandler or PostSubmitCallbackHandler)
2. Annotate the class with `@Component`
3. Implement the required methods to specify which events and states the handler supports
4. Add your business logic in the `handle` method

## Callback Execution

When multiple handlers can process the same callback:
1. Each handler receives the case data as modified by previous handlers
2. Validation errors from any handler will prevent submission
3. Validation success response from the callback will determine the success journey/path.

## Best Practices

1. Keep handlers focused on a single responsibility
2. Use clear naming conventions for handlers (e.g., `[Action]Handler`)
3. Write comprehensive unit tests for handlers
4. Document the purpose and behavior of each handler
5. Consider performance implications for complex handlers 
