package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.sir.core.Either
import ru.sir.core.None
import ru.sir.core.UseCase

class IsWorkspaceInit(val repository: Repository) : UseCase<Boolean, None, None>() {
    override fun run(params: None): Either<None, Boolean> = repository.isWorkspaceInit()
}