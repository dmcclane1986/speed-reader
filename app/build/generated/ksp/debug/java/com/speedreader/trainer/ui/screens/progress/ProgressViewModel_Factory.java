package com.speedreader.trainer.ui.screens.progress;

import com.speedreader.trainer.data.repository.ReadingSessionRepository;
import com.speedreader.trainer.data.repository.UserRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class ProgressViewModel_Factory implements Factory<ProgressViewModel> {
  private final Provider<ReadingSessionRepository> sessionRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public ProgressViewModel_Factory(Provider<ReadingSessionRepository> sessionRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public ProgressViewModel get() {
    return newInstance(sessionRepositoryProvider.get(), userRepositoryProvider.get());
  }

  public static ProgressViewModel_Factory create(
      Provider<ReadingSessionRepository> sessionRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new ProgressViewModel_Factory(sessionRepositoryProvider, userRepositoryProvider);
  }

  public static ProgressViewModel newInstance(ReadingSessionRepository sessionRepository,
      UserRepository userRepository) {
    return new ProgressViewModel(sessionRepository, userRepository);
  }
}
