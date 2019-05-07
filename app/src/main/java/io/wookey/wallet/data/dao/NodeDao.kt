package io.wookey.wallet.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import io.wookey.wallet.data.entity.Node

@Dao
interface NodeDao {

    /**
     * Insert nodes in the database. If the nodes already exists, ignore it.
     *
     * @param nodes the nodes to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNodes(vararg nodes: Node)

    /**
     * Insert nodes in the database. If the nodes already exists, ignore it.
     *
     * @param nodes the nodes to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNodesReplace(vararg nodes: Node)

    /**
     * Select all nodes is selected from the nodes table.
     *
     * @return all nodes is selected.
     */
    @Query("SELECT * FROM nodes WHERE isSelected = 1")
    fun loadSelectedNodes(): LiveData<List<Node>>

    /**
     * Select all nodes is symbol from the nodes table.
     *
     * @return all nodes is symbol.
     */
    @Query("SELECT * FROM nodes WHERE symbol = :symbol ORDER BY _id")
    fun loadSymbolNodes(symbol: String): LiveData<List<Node>>

    /**
     * Select all nodes is symbol from the nodes table.
     *
     * @return all nodes is symbol.
     */
    @Query("SELECT * FROM nodes WHERE symbol = :symbol ORDER BY _id")
    fun getSymbolNodes(symbol: String): List<Node>?

    /**
     * Select the node is symbol and isSelected from the nodes table.
     *
     * @return node is symbol and isSelected.
     */
    @Query("SELECT * FROM nodes WHERE symbol = :symbol AND isSelected = 1")
    fun getSymbolNode(symbol: String): Node?

    /**
     * Select the node is symbol and isSelected from the nodes table.
     *
     * @return node is symbol and isSelected.
     */
    @Query("SELECT * FROM nodes WHERE symbol = :symbol AND isSelected = 1")
    fun loadSymbolNode(symbol: String): LiveData<Node>

    /**
     * Update nodes in the database
     *
     * @param nodes the nodes to be updated.
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateNodes(vararg nodes: Node)

    /**
     * Delete node in the database
     *
     * @param node the node to be deleted.
     */
    @Delete
    fun deleteNode(node: Node)
}