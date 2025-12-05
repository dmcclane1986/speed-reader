package com.speedreader.trainer.ui.screens.quiz;

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
public final class ComprehensionQuizViewModel_Factory implements Factory<ComprehensionQuizViewModel> {
  private final Provider<ReadingSessionRepository> sessionRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public ComprehensionQuizViewModel_Factory(
      Provider<ReadingSessionRepository> sessionRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public ComprehensionQuizViewModel get() {
    return newInstance(sessionRepositoryProvider.get(), userRepositoryProvider.get());
  }

  public static ComprehensionQuizViewModel_Factory create(
      Provider<ReadingSessionRepository> sessionRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new ComprehensionQuizViewModel_Factory(sessionRepositoryProvider, userRepositoryProvider);
  }

  public static ComprehensionQuizViewModel newInstance(ReadingSessionRepository sessionRepository,
      UserRepository userRepository) {
    return new ComprehensionQuizViewModel(sessionRepository, userRepository);
  }
}
