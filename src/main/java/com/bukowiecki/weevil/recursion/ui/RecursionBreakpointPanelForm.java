/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.recursion.ui;

import javax.swing.*;

/**
 * @author Marcin Bukowiecki
 */
public class RecursionBreakpointPanelForm {
    private JPanel mainPanel;
    private JTextField iterationTextField;
    private JCheckBox iterationsCheckBox;

    public JTextField getIterationTextField() {
        return iterationTextField;
    }

    public JCheckBox getIterationsCheckBox() {
        return iterationsCheckBox;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
