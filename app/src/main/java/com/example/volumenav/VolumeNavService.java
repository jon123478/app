package com.example.volumenav;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class VolumeNavService extends AccessibilityService {

    private static final String TAG = "VolumeNavService";
    
    // Key States
    private boolean isVolumeUpPressed = false;
    private boolean isVolumeDownPressed = false;
    private long volumeUpTime = 0;
    private long volumeDownTime = 0;
    
    // Configuration
    private static final long LONG_PRESS_TIMEOUT = 400; // ms
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not used
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service Interrupted");
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            
            long currentTime = System.currentTimeMillis();

            if (action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    if (!isVolumeUpPressed) {
                        isVolumeUpPressed = true;
                        volumeUpTime = currentTime;
                    }
                } else {
                    if (!isVolumeDownPressed) {
                        isVolumeDownPressed = true;
                        volumeDownTime = currentTime;
                    }
                }
                return true; // Consume event to prevent volume change
            } else if (action == KeyEvent.ACTION_UP) {
                
                // Check if BOTH were pressed recently (simple heuristic for Home)
                boolean bothPressed = isVolumeUpPressed && isVolumeDownPressed;
                
                // If this UP event completes a "Both Pressed" sequence
                if (bothPressed) {
                    performGlobalAction(GLOBAL_ACTION_HOME);
                    resetStates();
                    return true;
                }

                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    if (isVolumeUpPressed) { // Only if it was pressed down first
                        if (currentTime - volumeUpTime >= LONG_PRESS_TIMEOUT) {
                            handleLongPressUp();
                        } else {
                            moveFocus(false); // Previous
                        }
                    }
                    isVolumeUpPressed = false;
                } else {
                    if (isVolumeDownPressed) {
                        if (currentTime - volumeDownTime >= LONG_PRESS_TIMEOUT) {
                            performGlobalAction(GLOBAL_ACTION_BACK);
                        } else {
                            moveFocus(true); // Next
                        }
                    }
                    isVolumeDownPressed = false;
                }
                return true;
            }
        }
        return super.onKeyEvent(event);
    }

    private void resetStates() {
        isVolumeUpPressed = false;
        isVolumeDownPressed = false;
    }

    private void moveFocus(boolean forward) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        AccessibilityNodeInfo currentFocus = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (currentFocus == null) {
            // If no focus, try to focus the root to start traversal
            currentFocus = root; 
        }

        // Determine direction
        int direction = forward ? AccessibilityNodeInfo.FOCUS_FORWARD : AccessibilityNodeInfo.FOCUS_BACKWARD;
        
        // Find next node
        AccessibilityNodeInfo nextNode = currentFocus.focusSearch(direction);
        
        if (nextNode != null) {
            // Perform focus action
            boolean focused = nextNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            if (!focused) {
                // Fallback: Accessibility Focus (sometimes needed if Input focus fails)
                nextNode.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }
            nextNode.recycle();
        }
        
        if (currentFocus != null && currentFocus != root) {
            currentFocus.recycle();
        }
        // root.recycle(); // Warning: recycling root here might be unsafe if currentFocus == root, but usually safer to just let garbage collection handle root in this brief scope or recycle if we acquired it. Correct practice is to recycle.
        if (currentFocus != root) root.recycle();
    }

    private void handleLongPressUp() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        
        AccessibilityNodeInfo focus = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (focus != null) {
            boolean clicked = focus.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (!clicked) {
                // Try parent if direct click fails
                AccessibilityNodeInfo parent = focus.getParent();
                if (parent != null) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    parent.recycle();
                }
            }
            focus.recycle();
        }
        root.recycle();
    }
}
