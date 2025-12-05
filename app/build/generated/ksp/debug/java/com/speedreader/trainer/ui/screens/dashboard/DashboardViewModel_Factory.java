package com.speedreader.trainer.ui.screens.dashboard;

import com.speedreader.trainer.data.repository.DocumentRepository;
import com.speedreader.trainer.data.repository.ReadingSessionRepository;
import com.speedreader.trainer.data.repository.SettingsRepository;
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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<DocumentRepository> documentRepositoryProvider;

  private final Provider<ReadingSessionRepository> sessionRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public DashboardViewModel_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<DocumentRepository> documentRepositoryProvider,
      Provider<ReadingSessionRepository> sessionRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.documentRepositoryProvider = documentRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(userRepositoryProvider.get(), documentRepositoryProvider.get(), sessionRepositoryProvider.get(), settingsRepositoryProvider.get());
  }

  public static DashboardViewModel_Factory create(Provider<UserRepository> userRepositoryProvider,
      Provider<DocumentRepository> documentRepositoryProvider,
      Provider<ReadingSessionRepository> sessionRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new DashboardViewModel_Factory(userRepositoryProvider, documentRepositoryProvider, sessionRepositoryProvider, settingsRepositoryProvider);
  }

  public static DashboardViewModel newInstance(UserRepository userRepository,
      DocumentRepository documentRepository, ReadingSessionRepository sessionRepository,
      SettingsRepository settingsRepository) {
    return new DashboardViewModel(userRepository, documentRepository, sessionRepository, settingsRepository);
  }
}
