# Gear Shop Screen Warning Audit

This document summarizes the IntelliJ inspections that were triggered while
working on `GearShopScreen` and explains whether each warning requires code
changes.

## Addressed warnings

| Warning message | Resolution | Notes |
| --- | --- | --- |
| `Statement lambda can be replaced with expression lambda` | Converted the inventory listeners in `GearShopScreenHandler` to method references so the lambda expression noise is removed. | No behavioural change – simply cleaner syntax. |
| `Condition 'scale != 1.0F' is always 'true'` | Simplified the buy-button label rendering path to always apply the scale transform, which eliminates the redundant branch. | The translation math now works for both scaled and unscaled labels. |
| `Result of 'max' is the same as the first argument making the call meaningless`<br>`Condition 'maxScrollSteps > 0' is always 'true'` | Tightened the scrolling helpers so they only run when the list can actually scroll. We now divide by `maxScrollSteps` directly and document why zero is impossible in that branch. | The guard clauses still prevent divide-by-zero issues. |
| `Condition 'row < 0' is always 'false'` | Removed redundant negative-row checks. Earlier bounds checks already ensured the row index cannot be negative. | The logic is clearer and the redundant comparisons disappear. |

## Warnings that are acceptable (no change)

| Warning message | Reasoning |
| --- | --- |
| `Calls to boolean method 'canScroll()' are always inverted` | Each call site intentionally uses `!canScroll()` so the method acts as a guard clause. In the positive branch `canScroll()` is known to be `true`, which is why the IDE thinks it is inverted. The logic is correct and more readable this way. |
| "Value of parameter '…' is always …" in the layout builder lambdas | Each builder invocation explicitly documents which constant governs a coordinate or spacing. Although the IDE flags the parameters as constant, the builder API is designed so layouts can override the values later. Keeping the constants in one place makes it easier to tweak page layouts without hunting through multiple methods. |
| `Return value of the method is never used` for builder calls | The builder methods return `this` to support fluent chaining. Some call sites prefer statement style for readability, so the return value is intentionally ignored. The fluent contract is used in other places (e.g. `PageLayout.defaults()`), so changing the signature to `void` would break that idiom. |

If new warnings appear that do not fall into these categories, give them a second
look – they may uncover a genuine bug.
