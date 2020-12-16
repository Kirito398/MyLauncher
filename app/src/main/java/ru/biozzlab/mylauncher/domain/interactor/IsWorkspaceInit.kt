package ru.biozzlab.mylauncher.domain.interactor

import ru.biozzlab.mylauncher.domain.interfaces.Repository
import ru.bis.entities.Either
import ru.bis.entities.None
import ru.bis.entities.UseCase

class IsWorkspaceInit(val repository: Repository) : UseCase<Boolean, None, None>() {
    override fun run(params: None): Either<None, Boolean> = repository.isWorkspaceInit()
}