/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.views;

import javax.swing.*;

/**
 * @author Marcin Bukowiecki
 */
public class EvaluateViewForm {
    private JPanel mainPanel;
    private JPanel optionPanel;
    private JButton evaluateButton;
    private JCheckBox codeSourceCheckBox;
    private JPanel threadComboBoxPanel;
    private JPanel buttonsPanel;
    private JCheckBox showMethodReturnValuesCheckBox;
    private JPanel settingsPanel;
    private JPanel otherOptionsPanel;
    private JCheckBox showSingleExpressionValuesCheckBox;

    public JPanel getOtherOptionsPanel() {
        return otherOptionsPanel;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JPanel getOptionPanel() {
        return optionPanel;
    }

    public JButton getEvaluateButton() {
        return evaluateButton;
    }

    public JCheckBox getCodeSourceCheckBox() {
        return codeSourceCheckBox;
    }

    public JCheckBox getShowMethodReturnValuesCheckBox() {
        return showMethodReturnValuesCheckBox;
    }

    public JCheckBox getShowSingleExpressionValuesCheckBox() {
        return showSingleExpressionValuesCheckBox;
    }

    public JPanel getThreadComboBoxPanel() {
        return threadComboBoxPanel;
    }
}
