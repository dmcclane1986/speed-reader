package com.speedreader.trainer.ui.screens.quiz;

import com.speedreader.trainer.data.repository.ReadingSessionRepository;
import com.speedreader.trainer.data.repository.SettingsRepository;
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
public final class SessionResultsViewModel_Factory implements Factory<SessionResultsViewModel> {
  private final Provider<ReadingSessionRepository> sessionRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public SessionResultsViewModel_Factory(
      Provider<ReadingSessionRepository> sessionRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public SessionResultsViewModel get() {
    return newInstance(sessionRepositoryProvider.get(), settingsRepositoryProvider.get());
  }

  public static SessionResultsViewModel_Factory create(
      Provider<ReadingSessionRepository> sessionRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new SessionResultsViewModel_Factory(sessionRepositoryProvider, settingsRepositoryProvider);
  }

  public static SessionResultsViewModel newInstance(ReadingSessionRepository sessionRepository,
      SettingsRepository settingsRepository) {
    return new SessionResultsViewModel(sessionRepository, settingsRepository);
  }
}
