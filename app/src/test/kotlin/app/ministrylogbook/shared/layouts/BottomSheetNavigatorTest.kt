package app.ministrylogbook.shared.layouts

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomSheetNavigatorTest {
    @Test
    fun shouldRequestUnlockForBottomSheetTarget_requestsUnlockOnlyWhenHiding() {
        assertTrue(shouldRequestUnlockForBottomSheetTarget(ModalBottomSheetValue.Hidden))
        assertFalse(shouldRequestUnlockForBottomSheetTarget(ModalBottomSheetValue.Expanded))
        assertFalse(shouldRequestUnlockForBottomSheetTarget(ModalBottomSheetValue.HalfExpanded))
    }
}
