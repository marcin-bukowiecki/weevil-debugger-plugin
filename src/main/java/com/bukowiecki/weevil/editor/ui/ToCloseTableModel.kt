/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.editor.ui

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.table.AbstractTableModel

/**
 * @author Marcin Bukowiecki
 */
class ToCloseTableModel(filesToClose: List<VirtualFile>) : AbstractTableModel() {

    private val myRows = filesToClose.map { ToCloseFileRow(it) }

    override fun getRowCount(): Int {
        return myRows.size
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        if (columnIndex == 0) return true
        return false
    }

    override fun getColumnName(column: Int): String {
        if (column == 0) {
            return ""
        }
        if (column == 1) {
            return WeevilDebuggerBundle.getMessage("weevil.debugger.file.path")
        }
        return super.getColumnName(column)
    }

    override fun getColumnCount(): Int = 2

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return myRows[rowIndex].getColumn(columnIndex)
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        if (columnIndex == 0) {
            myRows[rowIndex].myChecked = aValue as? Boolean ?: true
        }
        super.setValueAt(aValue, rowIndex, columnIndex)
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return if (columnIndex == 0) {
            java.lang.Boolean::class.java
        } else {
            String::class.java
        }
    }

    fun isChecked(virtualFile: VirtualFile): Boolean {
        return myRows.firstOrNull { it.myVirtualFile.url == virtualFile.url }?.myChecked ?: false
    }
}