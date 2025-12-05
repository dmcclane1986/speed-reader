package com.speedreader.trainer.ui.screens.baseline;

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
public final class BaselineViewModel_Factory implements Factory<BaselineViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  public BaselineViewModel_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public BaselineViewModel get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static BaselineViewModel_Factory create(Provider<UserRepository> userRepositoryProvider) {
    return new BaselineViewModel_Factory(userRepositoryProvider);
  }

  public static BaselineViewModel newInstance(UserRepository userRepository) {
    return new BaselineViewModel(userRepository);
  }
}
