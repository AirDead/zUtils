package me.airdead.zutils.modules

import me.airdead.zutils.global.map.ListsMap
import me.airdead.zutils.global.map.Snowflake
import me.airdead.zutils.global.minecraft.ServerPlugin
import me.airdead.zutils.global.minecraft.TaskMoment


class ModulesManager(val plugin: ServerPlugin) {
    val modules = LinkedHashMap<String, PluginModule>()

    var isLoading = false
    var isUnloading = false

    fun getModule(moduleName: String): PluginModule? {
        return modules[moduleName]
    }

    fun register(vararg modules: PluginModule) {
        modules.forEach {
            this.modules[it.id] = it
        }
    }


    fun unregister(moduleName: String) {
        val module = getModule(moduleName)
        if (module != null) {
            unregister(module)
        }
    }

    fun unregister(module: PluginModule) {
        modules.remove(module.id)
    }

    fun loadAll() {
        isLoading = true
        val sortedModules = try {
            sortModules()
        } catch (e: Exception) {
            plugin.logger.severe("Error while sorting modules for loading: ${e.localizedMessage}")
            return
        }

        sortedModules.forEach { module ->
            updateState(module.id, State.LOADING)
            executeTasks(TaskMoment.BEFORE_LOAD, module.id)
            try {
                module.onLoad(plugin)
            } catch (e: Exception) {
                plugin.logger.severe("Failed to load module ${module.id}: ${e.localizedMessage}")
            }
            updateState(module.id, State.LOADED)
            executeTasks(TaskMoment.AFTER_LOAD, module.id)
        }
        isLoading = false
    }


    fun unloadAll() {
        isUnloading = true

        val sortedModules = try {
            sortModules().asReversed()
        } catch (e: Exception) {
            plugin.logger.severe("Error while sorting modules for unloading: ${e.localizedMessage}")
            return
        }

        sortedModules.forEach { module ->
            updateState(module.id, State.UNLOADING)
            executeTasks(TaskMoment.BEFORE_UNLOAD, module.id)
            try {
                module.onUnload(plugin)
            } catch (e: Exception) {
                plugin.logger.severe("Failed to unload module ${module.id}: ${e.localizedMessage}")
            }

            updateState(module.id, State.UNLOADED)
            executeTasks(TaskMoment.AFTER_UNLOAD, module.id)
        }
        isUnloading = false
    }


    data class TaskData(
        val moment: TaskMoment,
        override val id: String,
        val moduleId: String,
        val task: () -> Unit
    ) : Snowflake<String>

    val tasks = ListsMap<TaskMoment, TaskData>()
    fun addTask(moment: TaskMoment, taskId: String, moduleId: String, task: () -> Unit) {
        tasks.add(moment, TaskData(moment, taskId, moduleId, task))
        if (moment == TaskMoment.AFTER_LOAD && isLoaded(moduleId)) {
            task()
        }
    }

    private fun executeTasks(moment: TaskMoment, moduleId: String) {
        val tasks = tasks[moment]?.filter { it.moduleId == moduleId }
        tasks?.forEach {
            try {
                it.task()
            } catch (e: Throwable) {
                plugin.logger.severe("Error while executing task '${it.id}'!")
                e.printStackTrace()
            }
        }
    }




    val modulesState = HashMap<String, State>()

    fun getState(moduleName: String): State {
        return modulesState[moduleName] ?: State.UNLOADED
    }

    fun isLoaded(moduleName: String) = getState(moduleName) == State.LOADED
    fun isLoading(moduleName: String) = getState(moduleName) == State.LOADING
    fun isUnloading(moduleName: String) = getState(moduleName) == State.UNLOADING
    fun isUnloaded(moduleName: String) = getState(moduleName) == State.UNLOADED



    private fun updateState(moduleName: String, state: State) {
        modulesState[moduleName] = state
    }


    fun reloadAll() {
        unloadAll()
        loadAll()
    }

    enum class State {
        LOADING,
        LOADED,
        UNLOADED,
        UNLOADING
    }



    private fun sortModules(): List<PluginModule> {
        val dependencyMap = modules.values.associateBy { it.javaClass }
        val sortedModules = mutableListOf<PluginModule>()
        val visited = mutableSetOf<Class<*>>()

        fun visit(moduleClass: Class<*>) {
            if (!visited.contains(moduleClass)) {
                visited.add(moduleClass)
                val annotation = moduleClass.getAnnotation(DependencyOrder::class.java)
                annotation?.before?.forEach { before ->
                    dependencyMap[before.java]?.also { visit(it.javaClass) }
                }
                dependencyMap[moduleClass]?.also { sortedModules.add(it) }
                annotation?.after?.forEach { after ->
                    dependencyMap[after.java]?.also { visit(it.javaClass) }
                }
            }
        }

        modules.values.forEach { visit(it.javaClass) }
        return sortedModules
    }
}